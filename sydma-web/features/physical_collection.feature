@passing @dc2f
Feature: Physical collection Research Group has correct permission and is not browsable

Background: 
	Given I have the usual users and roles
	And I have the research subject code with code "1234" and name "some code"
	
Scenario: Researcher cannot see create physical collection tab
	Given I log in as "ictintersect2"
	Then I should not see "Describe Sydney Research Data"
	
Scenario: Research Manager can see physical collection
	Given I log in as "ictintersect5"
	And I am on the browse rds page
	When I follow "Describe Sydney Research Data"	
	Then I should be on the create sydney research data page	

Scenario: Research Manager can create physical collection
	Given I log in as "ictintersect5"
	And I am on the create sydney research data page
	Given I fill in "name" with "phycol1"
	Given I fill in "subjectCode" with "1234"  
	Given I fill in "principalInvestigator" with "ictintersect2"
	Given I fill in textarea "description" with "some description"
	And I press "submit"
	Then I should be on the browse rds page	

Scenario: Research Manager can see physical groups, projects, and collections	
	Given I have a physical collection research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	Given I log in as "ictintersect5"
	And I am on the browse rds page
	Then I should see "Research Group Test"
	When I select option "Research Group Test" from select "Research_Groups"
	Then I should see "Research Project Test"
	And I select option "Research Project Test" from select "Projects"
	Then I should see "Dataset 1"
	And I select option "Dataset 1" from select "Datasets"

Scenario: Physical Collection has no browse link	
	Given I have a physical collection research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	Given I log in as "ictintersect5"
	And I am on the browse rds page
	When I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	And I should not see "View Dataset"

Scenario: Physical Collection has no assign permission link	
	Given I have a physical collection research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	Given I log in as "ictintersect5"
	And I am on the browse rds page
	When I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	And I should not see "Assign group permissions"
	And I should not see "Assign project permissions"
	And I should not see "Assign dataset permissions"
	