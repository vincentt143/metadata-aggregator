@passing
Feature: Physical collection Research Group has correct permission and is not browsable

Background: 
	Given I have the usual users and roles
	And I have the research subject code with code "1234" and name "some code"
	

Scenario: Researcher cannot see create physical collection tab
	Given I log in as "researcher"
	Then I should not see "Describe Sydney Research Data"
	
Scenario: Research Manager can see
	Given I log in as "research_manager"
	And I am on the browse rds page
	When I follow "Describe Sydney Research Data"	
	Then I should be on the create sydney research data page	


Scenario: Research Manager can create physical collection
	Given I log in as "research_manager"
	And I have a UniKey user "unikey"
	And I am on the create sydney research data page
	Given I fill in "name" with "phycol1"
	Given I fill in "subjectCode" with "1234"  
	Given I fill in "principalInvestigator" with "unikey"
	Given I fill in textarea "description" with "some description"
	And I press "submit"
	Then I should be on the browse rds page	

Scenario: Physical Collection has no browse link	
	Given I have a physical collection research group called "Research Group Test"
	And user "researcher" has full access to group "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I log in as "researcher"
	And I am on the browse rds page
	Then I sleep for "1" seconds
	When I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	And I should not see "View Dataset"