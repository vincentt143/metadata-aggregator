@passing @dc2d
Feature: File Annotations

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And the group "Research Group Test" has a project called "Research Project Test"
	And the project "Research Project Test" has a dataset "Dataset 1"
	And the research group "Research Group Test" has directory "Group One Dir"
	And the research dataset "Dataset 1" has a directory
	And the dataset "Dataset 1" has directory "directoryA"
	And the dataset "Dataset 1" has directory "directoryB"
	And the dataset "Dataset 1" has file "testFile.txt" under directory "directoryA"
	
Scenario: Edit Access can create annotation
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"	
	And I am on the view dataset page for research dataset "Dataset 1"
	When I open directory "directoryA"
	And I select file "testFile.txt"	
	When I follow "Create Annotation"
	Then I should see "Create File/Directory Annotation"
	And I fill in textarea "annotation" with "Some annotation"
	And I press button with value "Create"
	And I wait for lightbox to close
	Then I should not see "Create Annotation"
	Then I should have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	
	And I should see "Some annotation"

Scenario: Edit Access can edit annotation
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"	
	And I am on the view dataset page for research dataset "Dataset 1"
	And I have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	And I open directory "directoryA"
	When I select file "testFile.txt"
	When I follow "Edit Annotation"
	Then I should see "Edit File/Directory Annotation"
	And I fill in textarea "annotation" with "Some annotation edited"
	And I press button with value "Edit"
	And I wait for lightbox to close
	When I select file "testFile.txt"	
	Then I should not see "Create Annotation"
	Then I should have an annotation "Some annotation edited" for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	
	
Scenario: Viewing Access cannot create annotation
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"	
	And I am on the view dataset page for research dataset "Dataset 1"
	When I open directory "directoryA"
	And I select file "testFile.txt"	
	Then I should not see "Create Annotation"			

Scenario: Viewing Access can see annotation
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	And I have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	And I am on the view dataset page for research dataset "Dataset 1"
	When I open directory "directoryA"
	And I select file "testFile.txt"	
	Then I should see "Some annotation"
	
Scenario: Annotation is compulsory
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"	
	And I am on the view dataset page for research dataset "Dataset 1"
	When I open directory "directoryA"
	And I select file "testFile.txt"	
	When I follow "Create Annotation"
	Then I should see "Create File/Directory Annotation"	
	And I press button with value "Create"
	Then I should see "required field"	

Scenario: Cannot create duplicate
	Given user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"
	And I have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	 	
	When I am on the create annotation page for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	Then I should see "Annotation for Dataset 1/directoryA/testFile.txt already exists"	


Scenario: Edit Access can delete annotation
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"	
	And I am on the view dataset page for research dataset "Dataset 1"
	And I have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	When I open directory "directoryA"
	And I select file "testFile.txt"	
	When I follow "Delete Annotation"
	Then I should see "Delete File/Directory Annotation"
	And I press button with value "Delete"
	And I wait for lightbox to close
	Then I should not see "Delete Annotation"
	And I should not have an annotation for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	
	And I should see "Create Annotation"

Scenario: Viewing Access cannot see delete annotation link
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"	
	And I am on the view dataset page for research dataset "Dataset 1"
	And I have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	When I open directory "directoryA"
	And I select file "testFile.txt"		
	Then I should not see "Delete Annotation"		

Scenario: Viewing Access cannot delete annotation	
	Given user "ictintersect2" has viewing access to group "Research Group Test"
	And I log in as "ictintersect2"
	And I have an annotation "Some annotation" for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	 	
	When I am on the delete annotation page for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	Then I should see "Access Denied"	

Scenario: File deletion deletes annotation	
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	And I have an annotation "Annotation A" for path "directoryA/testFile.txt" for research dataset "Dataset 1"
	And I have an annotation "Annotation B" for path "directoryA" for research dataset "Dataset 1" 	 	
	And I am on the view dataset page for research dataset "Dataset 1"	
	When I select directory "directoryA"
	Then I follow "Delete Directory"	
	Then I wait for lightbox to fully open
	Then I press "confirm_submit"	
	And I wait for lightbox to close
	And I should not have an annotation for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	
	And I should not have an annotation for path "directoryA" for research dataset "Dataset 1" 		
	
Scenario: File moving merges annotation	
	Given user "ictintersect2" has editing access to group "Research Group Test"
	And I log in as "ictintersect2"
	And the dataset "Dataset 1" has directory "directoryA" under directory "directoryB" 
	And I have an annotation "Directory A Source" for path "directoryA" for research dataset "Dataset 1"
	And I have an annotation "Directory A Dest" for path "directoryB/directoryA" for research dataset "Dataset 1"
	And I have an annotation "File Source" for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	 	
	And I am on the view dataset page for research dataset "Dataset 1"	
	When I open directory "directoryA"
	Then I drag file "directoryA" into "directoryB"
	And I wait for lightbox to fully open
	And I click "confirm_submit" in the ajax lightbox
	And I wait for lightbox to close
	Then I should have an annotation "Directory A Dest Directory A Source" for path "directoryB/directoryA" for research dataset "Dataset 1"
	And I should have an annotation "File Source" for path "directoryB/directoryA/testFile.txt" for research dataset "Dataset 1"	
	And I should not have an annotation for path "directoryA" for research dataset "Dataset 1" 		
	And I should not have an annotation for path "directoryA/testFile.txt" for research dataset "Dataset 1" 	
	