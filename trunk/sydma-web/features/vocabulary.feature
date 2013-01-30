@passing @dc2d @dc2f
Feature: Vocabulary feature

Background: 
	Given I have the usual users and roles
	And I have a research group called "Research Group Test"
	And user "ictintersect2" has full access to group "Research Group Test"
	And I log in as "ictintersect2"

Scenario: Empty vocabulary set should redirect me to create vocabulary
	And I select option "Research Group Test" from select "Research_Groups"	
	And I follow "Manage Vocabulary"
	Then I should see "Add Vocabulary Set"

Scenario: Adding keywords
	When I am on the manage vocabulary page for research group "Research Group Test"
	And I fill in "keyword" with "MyKeyword"
	And I press "submit"
	Then I should see "MyKeyword"

Scenario: Adding empty keyword
	When I am on the manage vocabulary page for research group "Research Group Test"
	And I press "submit"
	Then I should see "Keyword is a required field"

Scenario: Adding duplicate keyword
	When I am on the manage vocabulary page for research group "Research Group Test"
	And I fill in "keyword" with "DuplicateKeyword"
	And I press "submit"
	Then I fill in "keyword" with "DuplicateKeyword"
	And I press "submit"
	Then I should see "Keyword already exists in the vocabulary."

Scenario: Editing keyword
	When I am on the manage vocabulary page for research group "Research Group Test"
	And I fill in "keyword" with "Keyword"
	And I press "submit"
	Then I follow "Edit"
	Then I should see "Edit Vocabulary Entry"
	And I fill in "keyword" with "EditedKeyword"
	And I press "submit"
	Then I should see "EditedKeyword"

Scenario: Editing invalid keyword
	When I am on the manage vocabulary page for research group "Research Group Test"
	And I fill in "keyword" with "Keyword"
	And I press "submit"
	Then I follow "Edit"
	And I fill in "keyword" with ""
	And I press "submit"
	Then I should see "Keyword is a required field"

Scenario: Deleting keyword
	When I am on the manage vocabulary page for research group "Research Group Test"
	And I fill in "keyword" with "a2LaskQ"
	And I press "submit"
	Then I follow "Delete"
	Then I am on the manage vocabulary page for research group "Research Group Test"
	Then I should be on the manage vocabulary page for "Research Group Test"
	And I should not see "a2LaskQ"

Scenario: Deleting all keywords
	When I am on the manage vocabulary page for research group "Research Group Test"
	Then I should not see "Delete All"
	And I fill in "keyword" with "a2LaskQ"
	And I press "submit"
	Then I follow "Delete All"
	Then I should be on the delete vocabulary page for "Research Group Test"
	And I press "submit"
	Then I should be on the manage vocabulary page for "Research Group Test"
	And I should not see "a2LaskQ"