CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    routing_key VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notifications_event_type ON notifications (event_type);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_occurred_at ON notifications (occurred_at DESC);
