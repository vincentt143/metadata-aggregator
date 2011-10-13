Feature: Test advertising of datasets

Background: 
	Given I have the usual users and roles
	And I have an internal user "principalinvest"
	And I have a research group called "Research Group Test" with principal investigator "principalinvest"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the project "Research Project Test" has an advertised dataset "Dataset 2"
	And the project "Research Project Test" has a ready for advertising dataset "Dataset 3"
	And I have public access right option "Right 1" with description "Right 1 desc"
	And I have public access right option "Right 2" with description "Right 2 desc"

Scenario: Non-advertised dataset shows the Advertise link and correct message for researcher (for non PI)
	Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	Then I should see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "Raise your research profile, increase your Google ranking and promote your research. "

Scenario: Ready for advertising dataset doesn't show advertise link and shows correct message for researcher (for non PI)
	Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	Then I should not see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "This Dataset has been marked as ready for advertising, but has not been advertised by the Principal Investigator to the RDA yet."
		
Scenario: check group edit link works
	Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	And I sleep for "1" seconds
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Given I follow "Edit Research Group"
	Then I should be on the edit research group including id page
	
Scenario: check project edit link works
	Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	And I sleep for "1" seconds
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Given I follow "Edit Research Project"
	Then I should be on the edit research project including id page
	
Scenario: check dataset edit link works
	Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	And I sleep for "1" seconds
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Given I follow "Edit Research Dataset"
	Then I should be on the edit research dataset including id page
	
Scenario: Advertised dataset shows the correct message and doesn't show the advertise link for researcher (for non PI)
	Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 2" from select "Datasets"
	Then I should not see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "This Dataset has been advertised in the "
	
Scenario: Non-advertised dataset shows the Advertise link and message (for PI)
	Given I log in as "principalinvest"
	Then show me the page
	And I am on the browse rds page
	Then show me the page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	Then I should see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "Raise your research profile, increase your Google ranking and promote your research. "

Scenario: Ready for advertising dataset shows the Advertise link and message (for PI)
	Given I log in as "principalinvest"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	Then I should see "Advertise Dataset"
	And I should see "Reject Dataset Advertising"
	And I should see "This Dataset has been marked as ready for advertising, but has not been advertised by the Principal Investigator to the RDA yet."

Scenario: Advertised datasets show the correct message and don't show the advertise link (for PI)
	Given I log in as "principalinvest"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 2" from select "Datasets"
	Then I should not see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "This Dataset has been advertised in the "
	
Scenario: Publish an unadvertised dataset as a PI makes it published
    Given I log in as "principalinvest"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	And I sleep for "1" seconds
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Dataset 1" should be advertised
	And the dataset "Dataset 1" should have public access right "Right 1"
	
Scenario: Publish a ready for advertising dataset as a PI makes it published
    Given I log in as "principalinvest"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	When I follow "Advertise Dataset"
	And I sleep for "1" seconds
	Then I should be on the advertise dataset page for research dataset "Dataset 3"
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Dataset 3" should be advertised
	And the dataset "Dataset 3" should have public access right "Right 1"

Scenario: Publish a dataset as a normal user marks it as ready for advertising
    Given I log in as "researcher"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	And I sleep for "1" seconds
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Then I should see the expected fields displayed
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Dataset 1" should be ready for advertising
	And the dataset "Dataset 1" should have public access right "Right 1"
	
Scenario: Reject a ready for advertising dataset as a PI makes it not advertised
    Given I log in as "principalinvest"
	And I am on the browse rds page
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	When I follow "Reject Dataset Advertising"
	Then I should be on the browse rds page
	And the dataset "Dataset 3" should be not advertised
	
	