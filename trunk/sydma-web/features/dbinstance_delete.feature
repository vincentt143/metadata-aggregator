@passing @dc2d
Feature: DB Instance Deletion

Background: 
	Given I have the usual users and roles	
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I have a dbschema with name "schema1"
	And dataset "Dataset 1" has dbinstance with schema "schema1"
			
Scenario: View Access cannot see delete DB instance
	Given user "ictintersect2" has viewing access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "View Database Instance"
	And I should not see "Delete Database Instance"

Scenario: Edit Access cannot see delete DB instance
	Given user "ictintersect2" has editing access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "View Database Instance"
	And I should not see "Delete Database Instance"

Scenario: Viewing Access cannot delete DB instance
	Given user "ictintersect2" has viewing access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "delete db instance" page for research dataset "Dataset 1"
	Then I should see "Access Denied"
	
Scenario: Edit Access cannot delete DB instance
	Given user "ictintersect2" has editing access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "delete db instance" page for research dataset "Dataset 1"
	Then I should see "Access Denied"	

Scenario: Full Access can see delete DB instance
	Given user "ictintersect2" has full access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "View Database Instance"
	And I should see "Delete Database Instance"	
	When I follow "Delete Database Instance"
	Then I should see "Delete Database Instance"
			
Scenario: Cancelling on Delete Page does not delete database
	Given user "ictintersect2" has full access to group "Research Group Test"		
	And a db instance should exist in the database for dataset "Dataset 1"
	And I log in as "ictintersect2"
	When I am on the "delete db instance" page for research dataset "Dataset 1"
	And I press button with value "Cancel"	
	Then I should be on the "view db instance" page for research dataset "Dataset 1"
	And a db instance should exist in the database for dataset "Dataset 1"
	And db users should exist in the database for dataset "Dataset 1"	
	
Scenario: Full Access can delete DB instance but fails confirm db name
	Given user "ictintersect2" has full access to group "Research Group Test"	
	And I log in as "ictintersect2"
	And I am on the "delete db instance" page for research dataset "Dataset 1"
	And I fill in "confirmDbName" with "abc" 
	Then I press button with value "Delete"
	Then I should see "The database instance name does not match!"

Scenario: Full Access can delete DB instance with correct confirm db name
	Given user "ictintersect2" has full access to group "Research Group Test"	
	And I log in as "ictintersect2"
	And I am on the "delete db instance" page for research dataset "Dataset 1"
	And I fill in "confirmDbName" with dataset "Dataset 1" db instance name 
	Then I press button with value "Delete"
	Then I should be on the browse rds page
	And a db instance should not exist in the database for dataset "Dataset 1"
	And db users should not exist in the database for dataset "Dataset 1"	
			
	