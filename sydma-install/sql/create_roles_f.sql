--
-- I had to enter sydma.roles, sydma.user & sydma.users_roles for it to execute.
--


--
-- Create some users & roles 
--
DELETE FROM users_roles;
DELETE FROM roles;

-- roles
INSERT INTO roles(id,version,name,display_name) VALUES(1,1,'ROLE_ADMINISTRATOR','Administrator');
INSERT INTO roles(id,version,name,display_name) VALUES(2,1,'ROLE_RESEARCHER','Researcher');
INSERT INTO roles(id,version,name,display_name) VALUES(3,1,'ROLE_ICT_SUPPORT','ICT Support');
INSERT INTO roles(id,version,name,display_name) VALUES(4,1,'NO_ROLE','-None-');
INSERT INTO roles(id,version,name,display_name) VALUES(5,1,'ROLE_RESEARCH_DATA_MANAGER','Research Data Manager');
INSERT INTO roles(id,version,name,display_name) VALUES(6,1,'ACCEPTED_TC','Accepted Terms & Conditions');
INSERT INTO roles(id,version,name,display_name) VALUES(7,1,'ACTIVE','Active');
