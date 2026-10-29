package com.portal.rrp.controller;

import com.portal.rrp.model.ReturnRequest;
import com.portal.rrp.model.Status;
import com.portal.rrp.service.ReturnRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Controller
@RequestMapping("/requests")
public class ReturnRequestController {

    @Autowired
    private ReturnRequestService service;

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Loads a request; a normal user may only open requests they created.
    private ReturnRequest loadAuthorized(Long id, Authentication auth) {
        ReturnRequest r = service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!isAdmin(auth) && !auth.getName().equals(r.getCreatedBy())) {
            throw new AccessDeniedException("You can only access your own requests");
        }
        return r;
    }

    // Admin may edit any request; a normal user only their own, and only before review starts.
    private ReturnRequest loadEditable(Long id, Authentication auth) {
        ReturnRequest r = loadAuthorized(id, auth);
        if (!isAdmin(auth) && r.getStatus() != Status.REQUESTED) {
            throw new AccessDeniedException("This request is already being processed and can no longer be edited");
        }
        return r;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Authentication auth, Model model) {
        model.addAttribute("requests", service.searchFor(q, auth.getName(), isAdmin(auth)));
        model.addAttribute("q", q);
        model.addAttribute("admin", isAdmin(auth));
        return "requests-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("request", new ReturnRequest());
        return "request-form";
    }

    @PostMapping
    public String create(@ModelAttribute ReturnRequest request,
                         @RequestParam(value = "productImageFile", required = false) MultipartFile productImageFile,
                         @RequestParam(value = "billImageFile", required = false) MultipartFile billImageFile,
                         Authentication auth) throws IOException {
        // never trust these two from the form: owner comes from the login, status always starts at REQUESTED
        request.setCreatedBy(auth.getName());
        request.setStatus(Status.REQUESTED);

        if (productImageFile != null && !productImageFile.isEmpty()) {
            request.setProductImage(productImageFile.getBytes());
            request.setProductImageContentType(productImageFile.getContentType());
            request.setProductImageFileName(productImageFile.getOriginalFilename());
        }
        if (billImageFile != null && !billImageFile.isEmpty()) {
            request.setBillImage(billImageFile.getBytes());
            request.setBillImageContentType(billImageFile.getContentType());
            request.setBillImageFileName(billImageFile.getOriginalFilename());
        }

        service.save(request);
        return "redirect:/requests";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("request", loadAuthorized(id, auth));
        model.addAttribute("history", service.historyFor(id));
        model.addAttribute("admin", isAdmin(auth));
        return "request-detail";
    }

    @GetMapping("/{id}/product-image")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id, Authentication auth) {
        ReturnRequest r = loadAuthorized(id, auth);
        if (!r.hasProductImage()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (r.getProductImageContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(r.getProductImageContentType());
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (r.getProductImageFileName() != null ? r.getProductImageFileName() : "product-image") + "\"")
                .contentType(mediaType)
                .body(r.getProductImage());
    }

    @GetMapping("/{id}/bill-image")
    @ResponseBody
    public ResponseEntity<byte[]> getBillImage(@PathVariable Long id, Authentication auth) {
        ReturnRequest r = loadAuthorized(id, auth);
        if (!r.hasBillImage()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (r.getBillImageContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(r.getBillImageContentType());
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (r.getBillImageFileName() != null ? r.getBillImageFileName() : "bill-image") + "\"")
                .contentType(mediaType)
                .body(r.getBillImage());
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("request", loadEditable(id, auth));
        return "request-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute ReturnRequest formData,
                         @RequestParam(value = "productImageFile", required = false) MultipartFile productImageFile,
                         @RequestParam(value = "billImageFile", required = false) MultipartFile billImageFile,
                         Authentication auth) throws IOException {
        ReturnRequest existing = loadEditable(id, auth);
        existing.setProduct(formData.getProduct());
        existing.setReason(formData.getReason());
        existing.setAmount(formData.getAmount());
        existing.setCustomerName(formData.getCustomerName());

        if (productImageFile != null && !productImageFile.isEmpty()) {
            existing.setProductImage(productImageFile.getBytes());
            existing.setProductImageContentType(productImageFile.getContentType());
            existing.setProductImageFileName(productImageFile.getOriginalFilename());
        }
        if (billImageFile != null && !billImageFile.isEmpty()) {
            existing.setBillImage(billImageFile.getBytes());
            existing.setBillImageContentType(billImageFile.getContentType());
            existing.setBillImageFileName(billImageFile.getOriginalFilename());
        }

        service.save(existing);
        return "redirect:/requests/" + id;
    }

    // ADMIN only (enforced in SecurityConfig). The actor is the logged-in admin, not a typed-in name.
    @PostMapping("/{id}/advance")
    public String advance(@PathVariable Long id, Authentication auth) {
        service.advanceStatus(id, auth.getName());
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, Authentication auth) {
        service.rejectRequest(id, auth.getName());
        return "redirect:/requests/" + id;
    }
}
