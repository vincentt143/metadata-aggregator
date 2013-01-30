Feature: Access control

Background:
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory

@passing @dc2d @dc2f
Scenario: Edit Access can edit Group
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the page for research group "Research Group Test"
	Then I should not see "Access Denied"
	And I should see "Edit Research Group"
	
@passing @dc2d @dc2f
Scenario: Viewing Access cannot edit Group
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the page for research group "Research Group Test"
	Then I should see "Access Denied"
	
@passing @dc2d @dc2f
Scenario: Full Access can create project
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the create project page for research group "Research Group Test"
	Then I should not see "Access Denied"
	And I should see "Create Research Project"
	
@passing @dc2d @dc2f
Scenario: Editing Access cannot create project
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the create project page for research group "Research Group Test"
	Then I should see "Access Denied"
	
@passing @dc2d @dc2f
Scenario: Edit Access can edit Project
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the edit page for research project "Research Project Test"
	Then I should not see "Access Denied"
	And I should see "Edit Research Project"
	
@passing @dc2d @dc2f
Scenario: Viewing Access cannot edit Project
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the edit page for research project "Research Project Test"
	Then I should see "Access Denied"
	
@passing @dc2d @dc2f
Scenario: Viewing Access cannot edit Group
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the page for research group "Research Group Test"
	Then I should see "Access Denied"

@passing @dc2d @dc2f
Scenario: Full Access can create Dataset
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the create dataset page for research project "Research Project Test"
	Then I should not see "Access Denied"
	And I should see "Create Research Dataset"
	
@passing @dc2d
Scenario: Full Access Agriculture can create Database
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create db instance" page for research dataset "Dataset 1"
	Then I should not see "Access Denied"

@passing @dc2f
Scenario: Full Access Aggregator cannot create Database
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the "create db instance" page for research dataset "Dataset 1"
	Then I should see "Access Denied"

@passing @dc2d @dc2f
Scenario: Editing Access cannot create Dataset
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the create dataset page for research project "Research Project Test"
	Then I should see "Access Denied"
	
@passing @dc2d @dc2f
Scenario: Edit Access can edit Dataset
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the edit page for research dataset "Dataset 1"
	Then I should not see "Access Denied"
	And I should see "Edit Research Dataset"
	
@passing @dc2d @dc2f
Scenario: Viewing Access cannot edit Dataset
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the edit page for research dataset "Dataset 1"
	Then I should see "Access Denied"

@passing @dc2d @dc2f
Scenario: Viewing Access cannot view Permissions
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the view permission page for research group "Research Group Test"
	Then I should see "Access Denied"

@passing @dc2d @dc2f
Scenario: Edit Access can view Permissions  	
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	When I am on the view permission page for research group "Research Group Test"
	Then I should be on the view permission page for research group "Research Group Test"
	
