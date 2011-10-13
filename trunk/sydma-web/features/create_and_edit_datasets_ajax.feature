@passing
Feature: Creating and editing datasets

Background: 
	Given I have the usual users and roles	
	And I have the research subject code with code "99" and name "some code"
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And I have a building with name "Building 1" and code "A01"
	And I have a building with name "Building 5" and code "A05"
	And user "researcher" has full access to group "Research Group Test"
	And I log in as "researcher"
	
	
Scenario: Create a dataset successfully
	Given I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I follow "Create Dataset"
	And I should see "Create Research Dataset"
	When I fill in "name" with "My Name"
	And I fill in "subjectCode" with "99"
	And I select option "Building 5 - A05" from select "physicalLocation"
	And I fill in "additionalLocationInformation" with "Additional loc info"
	And I fill in textarea "description" with "My Desc"
	And I press button with value "Submit"
	Then I wait for lightbox to close
	Then I should be on the browse rds page
	And dataset "My Name" should have subject code "99"
	And dataset "My Name" should have physical location "Building 5"
	And dataset "My Name" should have additional location information "Additional loc info"
	And dataset "My Name" should have description "My Desc"
	
Scenario: Edit a dataset successfully
	Given I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	And I follow "Edit Dataset"
	And I should see "Edit Research Dataset"
	When I fill in "name" with "My Name"
	And I fill in "subjectCode" with "99"
	And I select option "Building 5 - A05" from select "physicalLocation"
	And I fill in "additionalLocationInformation" with "Additional loc info"
	And I fill in textarea "description" with "My Desc"
	And I press button with value "Submit"
	Then I wait for lightbox to close
	Then I should be on the browse rds page
	And dataset "My Name" should have subject code "99"
	And dataset "My Name" should have physical location "Building 5"
	And dataset "My Name" should have additional location information "Additional loc info"
	And dataset "My Name" should have description "My Desc"

Scenario: Create a dataset with no location information
	Given I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I follow "Create Dataset"
	And I should see "Create Research Dataset"
	When I fill in "name" with "My Name"
	And I fill in "subjectCode" with "99"
	And I fill in textarea "description" with "My Desc"
	And I press button with value "Submit"
	Then I wait for lightbox to close
	Then I should be on the browse rds page
	And dataset "My Name" should have subject code "99"
	And dataset "My Name" should have no physical location
	And dataset "My Name" should have additional location information ""
	And dataset "My Name" should have description "My Desc"
	
	
