
CREATE INDEX idx_status_history_application_id 
ON application_status_history (application_id);


--CREATE INDEX idx_status_history_app_entered 
--ON application_status_history (application_id, entered_at DESC);


--CREATE INDEX idx_offers_application_id 
--ON offers (application_id);


--CREATE INDEX idx_offers_app_status 
--ON offers (application_id, offer_status);

--DROP INDEX idx_status_history_application_id;


--DROP INDEX idx_status_history_app_entered;


--DROP INDEX idx_offers_application_id;


--DROP INDEX idx_offers_app_status;
