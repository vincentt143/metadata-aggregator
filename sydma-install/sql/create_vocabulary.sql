DELETE FROM master_vocabulary;
DELETE FROM master_vocabulary_term;

-- Master templates
INSERT INTO master_vocabulary(id,version,name) VALUES(1,0,'Empty');
INSERT INTO master_vocabulary(id,version,name) VALUES(2,0,'Animals');
INSERT INTO master_vocabulary(id,version,name) VALUES(3,0,'Agriculture');
-- Master vocabulary list
-- list of animals
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(1,'Cat',0,2);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(2,'Dog',0,2);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(3,'Bird',0,2);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(4,'Mouse',0,2);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(5,'Tiger',0,2);
-- list of agrilculture terms
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(6,'Absorbance',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(7,'Benzylpenicillin',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(8,'Ceftriaxone',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(9,'Ecrlichiosis',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(10,'Nerve Fibers',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(11,'Peat Soils',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(12,'Peroxide',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(13,'Photosynthesis',0,3);
INSERT INTO master_vocabulary_term(id,keyword,version,master_vocabulary) VALUES(14,'Radiology',0,3);
