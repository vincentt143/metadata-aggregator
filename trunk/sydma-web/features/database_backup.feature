@passing @dc2d
Feature: Research Database Backup

Background: 
	Given I have the usual users and roles	
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I have a dbschema with name "schema1"
	And dataset "Dataset 1" has dbinstance with schema "schema1"

Scenario: Full Access can backup database
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	And I fill in textarea "description" with "Backup description"
	And I press button with value "Backup"
	Then I should be on the "manage backup" page for research dataset "Dataset 1"
	Then I should see "Backup description"
	
Scenario: Edit Access can backup database
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	And I fill in textarea "description" with "Backup description"
	And I press button with value "Backup"
	Then I should be on the "manage backup" page for research dataset "Dataset 1"
	Then I should see "Backup description"
	
Scenario: View Access cannot backup database
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	Then I should see "Access Denied"
	
Scenario: Restoring database without reason
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	And I fill in textarea "description" with "Backup description"
	And I press button with value "Backup"
	Then I should be on the "manage backup" page for research dataset "Dataset 1"
	When I follow "Restore"
	And I press button with value "Restore"
	Then I should see "This is a required field" 

Scenario: Restoring database without backup
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	And I fill in textarea "description" with "Backup description"
	And I press button with value "Backup"
	Then I should be on the "manage backup" page for research dataset "Dataset 1"
	When I follow "Restore"
	And I fill in textarea "state" with "No backup"
	And I press button with value "Restore"
	Then the dataset "Dataset 1" db state should have state "No backup"

Scenario: Restoring database with backup
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	And I fill in textarea "description" with "Backup description"
	And I press button with value "Backup"
	Then I should be on the "manage backup" page for research dataset "Dataset 1"
	When I follow "Restore"
	And I fill in textarea "state" with "Has backup"
	And I tick checkbox "backup"
	And I press button with value "Restore"
	Then the dataset "Dataset 1" db state should have state "Has backup"
	And I should see "This backup was performed before restoring this database. Reason: Has backup"

Scenario: Viewing a database instance after restoring
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create backup" page for research dataset "Dataset 1"
	And I fill in textarea "description" with "Backup description"
	And I press button with value "Backup"
	Then I should be on the "manage backup" page for research dataset "Dataset 1"
	When I follow "Restore"
	And I fill in textarea "state" with "Has backup"
	And I tick checkbox "backup"
	And I press button with value "Restore"
	Then the dataset "Dataset 1" db state should have state "Has backup"
	And I should see "This backup was performed before restoring this database. Reason: Has backup"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should see "Has backup"
	And I should see "given surname"