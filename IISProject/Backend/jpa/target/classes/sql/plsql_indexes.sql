
CREATE INDEX idx_status_history_application 
ON application_status_history USING btree (application_id);


CREATE INDEX idx_status_history_entered 
ON application_status_history USING btree (entered_at);


CREATE INDEX idx_status_history_exited 
ON application_status_history USING btree (exited_at);


CREATE INDEX idx_applications_applied_at 
ON applications USING btree (applied_at);


CREATE INDEX idx_applications_status_date 
ON applications USING btree (application_status, applied_at);



CREATE INDEX idx_offers_application 
ON offers USING btree (application_id);


CREATE INDEX idx_offers_created 
ON offers USING btree (created_at);


