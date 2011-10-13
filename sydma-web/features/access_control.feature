@passing
Feature: Access control

Background: 
	Given I have the usual users and roles	
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory

Scenario: Edit Access can edit Group
	Given user "researcher" has editing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the page for research group "Research Group Test"
	Then I should not see "Access Denied"
	And I should see "Edit Research Group"
	
Scenario: Viewing Access cannot edit Group
	Given user "researcher" has viewing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the page for research group "Research Group Test"
	Then I should see "Access Denied"
	
Scenario: Full Access can create project
	Given user "researcher" has full access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the create project page for research group "Research Group Test"
	Then I should not see "Access Denied"
	And I should see "Create Research Project"
	
Scenario: Editing Access cannot create project
	Given user "researcher" has editing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the create project page for research group "Research Group Test"
	Then I should see "Access Denied"
	
Scenario: Edit Access can edit Project
	Given user "researcher" has editing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the edit page for research project "Research Project Test"
	Then I should not see "Access Denied"
	And I should see "Edit Research Project"
	
Scenario: Viewing Access cannot edit Project
	Given user "researcher" has viewing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the edit page for research project "Research Project Test"
	Then I should see "Access Denied"
	
Scenario: Viewing Access cannot edit Group
	Given user "researcher" has viewing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the page for research group "Research Group Test"
	Then I should see "Access Denied"

Scenario: Full Access can create Dataset
	Given user "researcher" has full access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the create dataset page for research project "Research Project Test"
	Then I should not see "Access Denied"
	And I should see "Create Research Dataset"
	
Scenario: Editing Access cannot create Dataset
	Given user "researcher" has editing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the create dataset page for research project "Research Project Test"
	Then I should see "Access Denied"
	
Scenario: Edit Access can edit Dataset
	Given user "researcher" has editing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the edit page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "Edit Research Dataset"
	
Scenario: Viewing Access cannot edit Dataset
	Given user "researcher" has viewing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the edit page for research dataset "Dataset 1"
	Then I should see "Access Denied"

Scenario: Viewing Access cannot view Permissions
	Given user "researcher" has viewing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the view permission page for research group "Research Group Test"
	Then I should see "Access Denied"

Scenario: Edit Access can view Permissions  	
	Given user "researcher" has editing access to group "Research Group Test"
	And I log in as "researcher"
	When I am on the view permission page for research group "Research Group Test"
	Then I should see "Viewing Permissions"
	