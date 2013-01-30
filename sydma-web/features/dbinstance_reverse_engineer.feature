@passing @dc2d
Feature: DB Instance Reverse Engineering

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I have a dbschema with name "schema1"
	And dataset "Dataset 1" has dbinstance with schema "schema1"
	And dataset "Dataset 1" has a table "TestTable"
	And dataset "Dataset 1" table "TestTable" has a column called "test_column_one"
	And dataset "Dataset 1" table "TestTable" has a column called "test_column_two"
	
Scenario: The Reverse engineer link takes you to the right page
	Given user "ictintersect2" has full access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "view db instance" page for research dataset "Dataset 1"
	Then I should see "Reverse Engineer"
	When I follow "Reverse Engineer"
	Then I should be on the "reverse engineer" page for research dataset "Dataset 1"

Scenario: The Reverse engineer page shows the correct DLL
	Given user "ictintersect2" has full access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "reverse engineer" page for research dataset "Dataset 1"
	Then element with id "reversedDll" contains text "CREATE TABLE `TestTable` ("
	And element with id "reversedDll" contains text "test_column_one"
	And element with id "reversedDll" contains text "test_column_two"

Scenario: Saving the schema to a new one creates new schema
	Given user "ictintersect2" has full access to group "Research Group Test"	
	And I log in as "ictintersect2"
	When I am on the "reverse engineer" page for research dataset "Dataset 1"
	And I fill in "name" with "schema name"
	And I press button with value "Reverse"
	Then I should have a dbschema with name "schema name"
			
