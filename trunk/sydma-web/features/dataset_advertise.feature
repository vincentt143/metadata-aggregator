Feature: Test advertising of datasets

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test" with principal investigator "ictintersect3"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the project "Research Project Test" has an advertised dataset "Dataset 2"
	And the project "Research Project Test" has a ready for advertising dataset "Dataset 3"
	And I have public access right option "Right 1" with description "Right 1 desc"
	And I have public access right option "Right 2" with description "Right 2 desc"
	And user "ictintersect2" has full access to group "Research Group Test"
	And user "ictintersect3" has full access to group "Research Group Test"

@passing @dc2d @dc2f
Scenario: Non-advertised dataset shows the Advertise link and correct message for researcher (for non PI)
	Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	Then I should see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "Raise your research profile, increase your Google ranking and promote your research. "

@passing @dc2d @dc2f
Scenario: Ready for advertising dataset doesn't show advertise link and shows correct message for researcher (for non PI)
	Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	Then I should not see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "This Dataset has been marked as Ready for Advertising - Advertising to RDA has yet to be carried out by the Principal Investigator."

@passing @dc2d @dc2f
Scenario: check group edit link works
	Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Given I follow "Edit Research Group"
	Then I should see "Update Research Group"

@passing @dc2d @dc2f
Scenario: check project edit link works
	Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Given I follow "Edit Research Project"
	Then I should see "Update Research Project"

@passing @dc2d @dc2f
Scenario: check dataset edit link works
	Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Given I follow "Edit Research Dataset"
	Then I should see "Update Research Dataset"

@passing @dc2d @dc2f	
Scenario: Advertised dataset shows the correct message and doesn't show the advertise link for researcher (for non PI)
	Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 2" from select "Datasets"
	Then I should not see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "This Dataset has been advertised in the "

@passing @dc2d @dc2f	
Scenario: Non-advertised dataset shows the Advertise link and message (for PI)
	Given I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	Then I should see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "Raise your research profile, increase your Google ranking and promote your research. "

@passing @dc2d @dc2f
Scenario: Ready for advertising dataset shows the Advertise link and message (for PI)
	Given I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	Then I should see "Advertise Dataset"
	And I should see "Reject Dataset Advertising"
	And I should see "This Dataset has been marked as Ready for Advertising - Advertising to RDA has yet to be carried out by the Principal Investigator."

@passing @dc2d @dc2f
Scenario: Advertised datasets show the correct message and don't show the advertise link (for PI)
	Given I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 2" from select "Datasets"
	Then I should not see "Advertise Dataset"
	And I should not see "Reject Dataset Advertising"
	And I should see "This Dataset has been advertised in the "

@passing @dc2d @dc2f	
Scenario: Publish an unadvertised dataset as a PI makes it published
    Given I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Dataset 1" should be advertised
	And the dataset "Dataset 1" should have public access right "Right 1"

@passing @dc2d @dc2f
Scenario: Publish an unadvertised physical dataset as a Research Data Manager makes it published
	Given I have a physical collection research group called "Physical Group"
	And the group "Physical Group" has a project called "Physical Project"
	And the project "Physical Project" has a dataset "Physical Dataset"
    And I log in as "ictintersect5"
	And I am on the advertise dataset page for research dataset "Physical Dataset"
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Physical Dataset" should be advertised
	And the dataset "Physical Dataset" should have public access right "Right 1"

@passing @dc2d @dc2f
Scenario: Publish a ready for advertising dataset as a PI makes it published
    Given I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	When I follow "Advertise Dataset"
	Then I should be on the advertise dataset page for research dataset "Dataset 3"
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Dataset 3" should be advertised
	And the dataset "Dataset 3" should have public access right "Right 1"

@passing @dc2d @dc2f
Scenario: Publish a dataset as a normal user marks it as ready for advertising
    Given I log in as "ictintersect2"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 1" from select "Datasets"
	When I follow "Advertise Dataset"
	Then I should be on the advertise dataset page for research dataset "Dataset 1"
	Then I should see the expected fields displayed
	When I select option "Right 1" from select "publicAccessRight"
	And I press "advertise"	
	Then the dataset "Dataset 1" should be ready for advertising
	And the dataset "Dataset 1" should have public access right "Right 1"

@passing @dc2d @dc2f
Scenario: Reject a ready for advertising dataset as a PI makes it not advertised
    Given I log in as "ictintersect3"
	And I select option "Research Group Test" from select "Research_Groups"
	And I select option "Research Project Test" from select "Projects"
	And I select option "Dataset 3" from select "Datasets"
	When I follow "Reject Dataset Advertising"
	Then I should be on the browse rds page
	And the dataset "Dataset 3" should be not advertised