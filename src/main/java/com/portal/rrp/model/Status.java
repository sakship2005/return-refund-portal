package com.portal.rrp.model;

public enum Status {
    // the text is the label of the button that moves a request OUT of that status (null = end of workflow)
    REQUESTED("Start review"),
    UNDER_REVIEW("Approve"),
    APPROVED("Mark item received"),
    REJECTED(null),
    ITEM_RECEIVED("Process refund"),
    REFUND_PROCESSED("Close request"),
    CLOSED(null);

    private final String actionLabel;

    Status(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getActionLabel() {
        return actionLabel;
    }
}
