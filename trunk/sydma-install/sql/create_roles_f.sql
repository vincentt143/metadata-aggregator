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

--populate user tables with sample data - that allow simple run
INSERT INTO `users` (`id`, `email`, `enabled`, `givenname`, `has_rstudio_account`, `institution`, `password`, `surname`, `user_type`, `username`, `version`) VALUES (1,'admin@test.com','',NULL,NULL,NULL,'5f4dcc3b5aa765d61d8327deb882cf99',NULL,'INTERNAL','test',NULL);
INSERT INTO `users` (`id`, `email`, `enabled`, `givenname`, `has_rstudio_account`, `institution`, `password`, `surname`, `user_type`, `username`, `version`) VALUES (2,'test@test.com','',NULL,'\0',NULL,'unikey_user','user1','UNIKEY','user1',1);
INSERT INTO `users` (`id`, `email`, `enabled`, `givenname`, `has_rstudio_account`, `institution`, `password`, `surname`, `user_type`, `username`, `version`) VALUES (3,'test@test.com','',NULL,'\0',NULL,'unikey_user','user2','UNIKEY','user2',1);
INSERT INTO `users` (`id`, `email`, `enabled`, `givenname`, `has_rstudio_account`, `institution`, `password`, `surname`, `user_type`, `username`, `version`) VALUES (4,'test@test.com','',NULL,'\0',NULL,'unikey_user','user3','UNIKEY','user3',1);

INSERT INTO `users_roles` (`users`, `roles`) VALUES (1,1);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (1,2);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (1,3);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (1,5);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (1,6);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (1,7);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (2,2);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (2,6);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (2,7);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (3,2);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (3,6);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (3,7);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (4,2);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (4,6);
INSERT INTO `users_roles` (`users`, `roles`) VALUES (4,7);
