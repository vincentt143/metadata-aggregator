@passing @dc2d
Feature: Request RDS space

Background: 
	Given I have the usual users and roles
	And I have the research subject code with code "90" and name "some code"

Scenario: UniKey researchers can be set as PI
	Given I log in as "ictintersect2"
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "principalInvestigator" with "ictintersect3" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should see "CREATED"

Scenario: UniKey researchers can be set as DMC
	Given I log in as "ictintersect2"
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "principalInvestigator" with "ictintersect3" 
	And I fill in "dataManagementContact" with "ictintersect3" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should see "CREATED"

Scenario: UniKey is Mandatory
	Given I log in as "ictintersect2"
	Given I am on the request rds page
	And I fill in "name" with "Research Group" 
	And I fill in "subjectCode" with "90" 
	And I fill in "amountOfStorage" with "1" 
	And I fill in textarea "description" with "description"
	And I press "submit"
	Then I should not see "CREATED"
	And I should see "Principal Investigator is a required field"
	
Scenario: Approved requests give full access to Principal Investigator
	Given I log in as "ictintersect4"
	And I have an unapproved RDS request called "Unapproved" with PI "ictintersect2"
	And there is a directory called "rg1"
	When I am on the Unapproved RDS requests page
	And I follow "Approve"
	And I fill in "dirPath" with "rg1"
	And I press "approve_submit"
	And user "ictintersect2" should have full access to group "Unapproved"
	
Scenario: Check for info on RDS page
	Given I log in as "ictintersect4"
	And I have an unapproved RDS request called "Unapproved" with PI "ictintersect2" and date "10-11-11T09:36:24" and requester "ictintersect3"
	When I am on the Unapproved RDS requests page
	Then I should see "Unapproved" in the cell "requestName" in the table "requestList"
	Then I should see "10-11-11T09:36:24" in the cell "requestDate" in the table "requestList"
	Then I should see "ictintersect3" in the cell "requester" in the table "requestList"
	Then I should see "ictintersect2" in the cell "principalInvestigator" in the table "requestList"
	
Scenario: Editing the PI and space required in an RDS request
	Given I log in as "ictintersect4"
	And I have an unapproved RDS request called "Unapproved" with PI "ictintersect2"
	And there is a directory called "rg1"
	When I am on the Unapproved RDS requests page
	And I follow "Approve"	
	And I fill in "dirPath" with "rg1"
	Then I should see "100"
	And I should see "ictintersect2"
	And I fill in "principalInvestigator" with "ictintersect3"
	And I fill in "amountOfStorage" with "200"
	And I press "approve_submit"
	Then I should see "List of Approved Research Group Requests"
	And I should see "Unapproved"
	And user "ictintersect3" should have full access to group "Unapproved"

Scenario: Editing the PI and space required with incorrect values
	Given I log in as "ictintersect4"
	And I have an unapproved RDS request called "Unapproved" with PI "ictintersect2"
	And there is a directory called "rg1"
	When I am on the Unapproved RDS requests page
	And I follow "Approve"	
	And I fill in "dirPath" with "rg1"
	Then I should see "100"
	And I should see "ictintersect2"
	And I fill in "principalInvestigator" with "noneUnikey"
	And I fill in "amountOfStorage" with "200ert"
	And I press "approve_submit"
	And I should see "The Principal Investigator is not a UniKey user, or they have not yet logged in into the Research Data Manager"
	And I should see "Please enter a number"
	
Scenario: System admin can update PI and permissions are updated
	Given I log in as "ictintersect4"
	And I have an approved RDS request called "Approved" with PI "ictintersect2"
	When I am on the Approved RDS requests page
	Then I should see "Approved"
	And user "ictintersect2" should have full access to group "Approved"
	When I follow "Change PI"
	And I fill in "principalInvestigator" with "ictintersect3"
	And I press "update_submit"
	Then user "ictintersect3" should have full access to group "Approved"
	And user "ictintersect2" should not have access to group "Approved"