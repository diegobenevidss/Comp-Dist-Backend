CREATE TABLE triages (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    patient_name VARCHAR(150) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    chief_complaint TEXT NOT NULL,
    notes TEXT,
    attended_by VARCHAR(150),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attended_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_triages_patient_id ON triages (patient_id);
CREATE INDEX idx_triages_priority ON triages (priority);
CREATE INDEX idx_triages_status ON triages (status);
CREATE INDEX idx_triages_created_at ON triages (created_at DESC);
