package com.portal.rrp;

import com.portal.rrp.controller.ReturnRequestController;
import com.portal.rrp.model.ReturnRequest;
import com.portal.rrp.model.Status;
import com.portal.rrp.repository.ReturnRequestRepository;
import com.portal.rrp.repository.StatusHistoryRepository;
import com.portal.rrp.service.ReturnRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ImageUploadTest {

    private ReturnRequestController controller;
    private ReturnRequestService service;
    private Map<Long, ReturnRequest> store;
    private AtomicLong seq;

    private UsernamePasswordAuthenticationToken userAuth;
    private UsernamePasswordAuthenticationToken user2Auth;
    private UsernamePasswordAuthenticationToken adminAuth;

    @BeforeEach
    void setup() {
        store = new ConcurrentHashMap<>();
        seq = new AtomicLong(1);

        ReturnRequestRepository fakeRepo = (ReturnRequestRepository) Proxy.newProxyInstance(
                ReturnRequestRepository.class.getClassLoader(),
                new Class<?>[]{ReturnRequestRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        ReturnRequest r = (ReturnRequest) args[0];
                        if (r.getId() == null) {
                            r.setId(seq.getAndIncrement());
                        }
                        store.put(r.getId(), r);
                        return r;
                    }
                    if ("findById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        return Optional.ofNullable(store.get(id));
                    }
                    if ("findAll".equals(method.getName())) {
                        return new ArrayList<>(store.values());
                    }
                    return null;
                }
        );

        StatusHistoryRepository fakeHistoryRepo = (StatusHistoryRepository) Proxy.newProxyInstance(
                StatusHistoryRepository.class.getClassLoader(),
                new Class<?>[]{StatusHistoryRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        return args[0];
                    }
                    if ("findByRequestIdOrderByChangedAtAsc".equals(method.getName())) {
                        return Collections.emptyList();
                    }
                    return null;
                }
        );

        service = new ReturnRequestService();
        ReflectionTestUtils.setField(service, "repository", fakeRepo);
        ReflectionTestUtils.setField(service, "historyRepository", fakeHistoryRepo);

        controller = new ReturnRequestController();
        ReflectionTestUtils.setField(controller, "service", service);

        userAuth = new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        user2Auth = new UsernamePasswordAuthenticationToken(
                "user2", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        adminAuth = new UsernamePasswordAuthenticationToken(
                "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void createRequest_withProductAndBillImages_attachesFilesAndPersists() throws IOException {
        byte[] fakeProductImage = "fake-product-image-content".getBytes();
        byte[] fakeBillImage = "fake-bill-receipt-content".getBytes();

        MockMultipartFile productFile = new MockMultipartFile(
                "productImageFile",
                "phone_cracked.png",
                MediaType.IMAGE_PNG_VALUE,
                fakeProductImage
        );

        MockMultipartFile billFile = new MockMultipartFile(
                "billImageFile",
                "invoice_1092.pdf",
                "application/pdf",
                fakeBillImage
        );

        ReturnRequest req = new ReturnRequest();
        req.setOrderId("ORD-IMAGE-101");
        req.setProduct("Smart Phone");
        req.setReason("Screen cracked inside package");
        req.setAmount(19999.00);
        req.setCustomerName("Rahul Sharma");

        String redirect = controller.create(req, productFile, billFile, userAuth);
        assertEquals("redirect:/requests", redirect);

        // Verify request stored in repository
        assertNotNull(req.getId());
        ReturnRequest saved = store.get(req.getId());
        assertNotNull(saved);

        assertEquals("user", saved.getCreatedBy());
        assertEquals("Smart Phone", saved.getProduct());
        assertEquals(Status.REQUESTED, saved.getStatus());
        assertTrue(saved.hasProductImage());
        assertTrue(saved.hasBillImage());
        assertEquals("phone_cracked.png", saved.getProductImageFileName());
        assertEquals("invoice_1092.pdf", saved.getBillImageFileName());
        assertEquals(MediaType.IMAGE_PNG_VALUE, saved.getProductImageContentType());
        assertEquals("application/pdf", saved.getBillImageContentType());
        assertArrayEquals(fakeProductImage, saved.getProductImage());
        assertArrayEquals(fakeBillImage, saved.getBillImage());
    }

    @Test
    void getImages_creatorCanViewBothProductAndBill() {
        ReturnRequest req = new ReturnRequest();
        req.setId(101L);
        req.setOrderId("ORD-101");
        req.setProduct("Smart Phone");
        req.setCreatedBy("user");
        req.setProductImage("product-bytes".getBytes());
        req.setProductImageContentType("image/png");
        req.setProductImageFileName("product.png");
        req.setBillImage("bill-bytes".getBytes());
        req.setBillImageContentType("application/pdf");
        req.setBillImageFileName("invoice.pdf");
        store.put(101L, req);

        ResponseEntity<byte[]> prodResp = controller.getProductImage(101L, userAuth);
        assertEquals(HttpStatus.OK, prodResp.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, prodResp.getHeaders().getContentType());
        assertArrayEquals("product-bytes".getBytes(), prodResp.getBody());

        ResponseEntity<byte[]> billResp = controller.getBillImage(101L, userAuth);
        assertEquals(HttpStatus.OK, billResp.getStatusCode());
        assertEquals(MediaType.parseMediaType("application/pdf"), billResp.getHeaders().getContentType());
        assertArrayEquals("bill-bytes".getBytes(), billResp.getBody());
    }

    @Test
    void getImages_unauthorizedUser_isDeniedWith403() {
        ReturnRequest req = new ReturnRequest();
        req.setId(202L);
        req.setOrderId("ORD-SECRET-1");
        req.setCreatedBy("user"); // created by "user", requested by "user2"
        req.setProductImage("secret-product".getBytes());
        req.setBillImage("secret-bill".getBytes());
        store.put(202L, req);

        assertThrows(AccessDeniedException.class, () ->
                controller.getProductImage(202L, user2Auth));

        assertThrows(AccessDeniedException.class, () ->
                controller.getBillImage(202L, user2Auth));
    }

    @Test
    void adminCanInspectProofs_andDecideWhetherToApproveOrReject() {
        ReturnRequest req = new ReturnRequest();
        req.setId(303L);
        req.setOrderId("ORD-ADMIN-CHECK");
        req.setProduct("Headphones");
        req.setReason("Right side not working");
        req.setAmount(1200.0);
        req.setCustomerName("Ankit Gupta");
        req.setCreatedBy("user");
        req.setStatus(Status.REQUESTED);
        req.setProductImage("headphones-broken-bytes".getBytes());
        req.setProductImageContentType("image/png");
        req.setBillImage("invoice-bill-bytes".getBytes());
        req.setBillImageContentType("image/png");
        store.put(303L, req);

        // Admin can inspect product image and bill receipt
        ResponseEntity<byte[]> prodResp = controller.getProductImage(303L, adminAuth);
        assertEquals(HttpStatus.OK, prodResp.getStatusCode());
        assertArrayEquals("headphones-broken-bytes".getBytes(), prodResp.getBody());

        ResponseEntity<byte[]> billResp = controller.getBillImage(303L, adminAuth);
        assertEquals(HttpStatus.OK, billResp.getStatusCode());
        assertArrayEquals("invoice-bill-bytes".getBytes(), billResp.getBody());

        // Admin opens detail page
        Model model = new ConcurrentModel();
        String view = controller.view(303L, adminAuth, model);
        assertEquals("request-detail", view);
        assertTrue(model.containsAttribute("request"));
        assertEquals(true, model.getAttribute("admin"));

        // Admin advances to review: REQUESTED -> UNDER_REVIEW
        controller.advance(303L, adminAuth);
        assertEquals(Status.UNDER_REVIEW, req.getStatus());

        // Admin reviews the proofs and decides to reject
        controller.reject(303L, adminAuth);
        assertEquals(Status.REJECTED, req.getStatus());
    }

    @Test
    void updateRequest_allowsReplacingImagesOrPreservingExisting() throws IOException {
        ReturnRequest existing = new ReturnRequest();
        existing.setId(404L);
        existing.setOrderId("ORD-UPDATE");
        existing.setProduct("Old Product");
        existing.setReason("Old Reason");
        existing.setAmount(500.0);
        existing.setCustomerName("Customer A");
        existing.setCreatedBy("user");
        existing.setStatus(Status.REQUESTED);
        existing.setProductImage("old-product-image".getBytes());
        existing.setBillImage("old-bill-image".getBytes());
        store.put(404L, existing);

        ReturnRequest formData = new ReturnRequest();
        formData.setProduct("Updated Product");
        formData.setReason("Updated Reason");
        formData.setAmount(750.0);
        formData.setCustomerName("Customer A");

        // Submit update with new product image, but empty bill image (preserves old bill)
        MockMultipartFile newProductFile = new MockMultipartFile(
                "productImageFile", "new_pic.jpg", "image/jpeg", "new-product-bytes".getBytes());
        MockMultipartFile emptyBillFile = new MockMultipartFile(
                "billImageFile", "", "application/octet-stream", new byte[0]);

        String redirect = controller.update(404L, formData, newProductFile, emptyBillFile, userAuth);
        assertEquals("redirect:/requests/404", redirect);

        assertEquals("Updated Product", existing.getProduct());
        assertEquals("Updated Reason", existing.getReason());
        assertEquals(750.0, existing.getAmount());
        assertArrayEquals("new-product-bytes".getBytes(), existing.getProductImage());
        assertEquals("new_pic.jpg", existing.getProductImageFileName());
        assertEquals("image/jpeg", existing.getProductImageContentType());
        assertArrayEquals("old-bill-image".getBytes(), existing.getBillImage()); // preserved!
    }
}
