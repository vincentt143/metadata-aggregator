-- set up access right options
DELETE FROM public_access_right;
INSERT INTO public_access_right(short_name, description) VALUES ("Make research data publicly accessible", "Make research data publicly accessible");
INSERT INTO public_access_right(short_name, description) VALUES ("Contact researcher to request access", "Make research data accessible to individuals upon specific agreement with the researcher supplying the data");
