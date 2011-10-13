--
-- I had to enter sydma.roles, sydma.user & sydma.users_roles for it to execute.
--


--
-- Create some users & roles 
--
DELETE FROM users_roles;
DELETE FROM roles;
DELETE FROM users;

-- roles
INSERT INTO roles(id,version,name,display_name) VALUES(1,1,'ROLE_ADMINISTRATOR','Administrator');
INSERT INTO roles(id,version,name,display_name) VALUES(2,1,'ROLE_RESEARCHER','Researcher');
INSERT INTO roles(id,version,name,display_name) VALUES(3,1,'ROLE_ICT_SUPPORT','ICT Support');
INSERT INTO roles(id,version,name,display_name) VALUES(4,1,'NO_ROLE','-None-');
INSERT INTO roles(id,version,name,display_name) VALUES(5,1,'ROLE_RESEARCH_DATA_MANAGER','Research Data Manager');
INSERT INTO roles(id,version,name,display_name) VALUES(6,1,'ACCEPTED_TC','Accepted Terms & Conditions');
INSERT INTO roles(id,version,name,display_name) VALUES(7,1,'ACTIVE','Active');
-- users
-- local
INSERT INTO users(id,version,username,password,enabled,email,institution,givenname,surname,user_type) VALUES(1,1,'admin@intersect.org.au','21232f297a57a5a743894a0e4a801fc3',true,'admin@intersect.org.au',null,'admin','admin','INTERNAL');
INSERT INTO users(id,version,username,password,enabled,email,institution,givenname,surname,user_type) VALUES(2,1,'researcher@intersect.org.au','eac39bc377f76c083055c3069b9475d8',true,'researcher@intersect.org.au',null,'researcher','researcher','INTERNAL');
INSERT INTO users(id,version,username,password,enabled,email,institution,givenname,surname,user_type) VALUES(3,1,'support@intersect.org.au','434990c8a25d2be94863561ae98bd682',true,'support@intersect.org.au',null,'support','support','INTERNAL');
INSERT INTO users(id,version,username,password,enabled,email,institution,givenname,surname,user_type) VALUES(4,1,'all@intersect.org.au','434990c8a25d2be94863561ae98bd682',true,'all@intersect.org.au',null,'all','all','INTERNAL');
INSERT INTO users(id,version,username,password,enabled,email,institution,givenname,surname,user_type) VALUES(8,1,'researchmanager@intersect.org.au','eac39bc377f76c083055c3069b9475d8',true,'researchermanager@intersect.org.au',null,'researchmanager','researchmanager','INTERNAL');
-- unikey
INSERT INTO users(id,version,username,enabled,email,institution,givenname,surname,user_type) VALUES(5,1,'ictintersect4',true,'ict4@intersect.org.au',null,'ictfour','ictfour','UNIKEY');
INSERT INTO users(id,version,username,enabled,email,institution,givenname,surname,user_type) VALUES(6,1,'ictintersect5',true,'ict5@intersect.org.au',null,'ictfive','ictfive','UNIKEY');
INSERT INTO users(id,version,username,enabled,email,institution,givenname,surname,user_type) VALUES(7,1,'ictintersect3',true,'ict3@intersect.org.au',null,'ictthree','ictthree','UNIKEY');
INSERT INTO users(id,version,username,enabled,email,institution,givenname,surname,user_type) VALUES(9,1,'ictintersect2',true,'ict2@intersect.org.au',null,'icttwo','icttwo','UNIKEY');

-- map users to roles
INSERT INTO users_roles(users,roles) VALUES(1,1);
INSERT INTO users_roles(users,roles) VALUES(2,2);
INSERT INTO users_roles(users,roles) VALUES(3,3);
INSERT INTO users_roles(users,roles) VALUES(4,1);
INSERT INTO users_roles(users,roles) VALUES(4,2);
INSERT INTO users_roles(users,roles) VALUES(4,3);
INSERT INTO users_roles(users,roles) VALUES(5,1);
INSERT INTO users_roles(users,roles) VALUES(6,3);
INSERT INTO users_roles(users,roles) VALUES(7,2);
INSERT INTO users_roles(users,roles) VALUES(8,5);
INSERT INTO users_roles(users,roles) VALUES(8,2);
INSERT INTO users_roles(users,roles) VALUES(9,1);
INSERT INTO users_roles(users,roles) VALUES(9,2);
INSERT INTO users_roles(users,roles) VALUES(9,3);
INSERT INTO users_roles(users,roles) VALUES(9,4);
-- Users have accepted TC
INSERT INTO users_roles(users,roles) VALUES(1,6);
INSERT INTO users_roles(users,roles) VALUES(2,6);
INSERT INTO users_roles(users,roles) VALUES(3,6);
INSERT INTO users_roles(users,roles) VALUES(4,6);
INSERT INTO users_roles(users,roles) VALUES(5,6);
INSERT INTO users_roles(users,roles) VALUES(6,6);
INSERT INTO users_roles(users,roles) VALUES(7,6);
INSERT INTO users_roles(users,roles) VALUES(8,6);
INSERT INTO users_roles(users,roles) VALUES(9,6);
-- Users are active
INSERT INTO users_roles(users,roles) VALUES(1,7);
INSERT INTO users_roles(users,roles) VALUES(2,7);
INSERT INTO users_roles(users,roles) VALUES(3,7);
INSERT INTO users_roles(users,roles) VALUES(4,7);
INSERT INTO users_roles(users,roles) VALUES(5,7);
INSERT INTO users_roles(users,roles) VALUES(6,7);
INSERT INTO users_roles(users,roles) VALUES(7,7);
INSERT INTO users_roles(users,roles) VALUES(8,7);
INSERT INTO users_roles(users,roles) VALUES(9,7);