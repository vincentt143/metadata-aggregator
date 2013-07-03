<?xml version="1.0" encoding="UTF-8"?>
<registryObjects xmlns="http://ands.org.au/standards/rif-cs/registryObjects"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://ands.org.au/standards/rif-cs/registryObjects http://services.ands.org.au/documentation/rifcs/1.3/schema/registryObjects.xsd">
<registryObject group="$researchDataset.researchProject.researchGroup.name$">
    <key>$researchDataset.keyForRifCs$</key>
    <originatingSource>www.sydney.edu.au</originatingSource>
    <collection type="dataset">
        <identifier type="local">dataset-$researchDataset.id$</identifier>

        <name type="primary">
            <namePart>$researchDataset.name$</namePart>
        </name>
        
        $if(hasLocation)$
        <location>
            <address>
                <physical type="streetAddress">
                    <addressPart type="addressLine">$buildingName$</addressPart>
                    $if(additionalLocationInformation)$
                    <addressPart type="addressLine">$additionalLocationInformation$</addressPart>
                    $endif$
                    $if(address1)$
                    <addressPart type="addressLine">$address1$</addressPart>
                    $endif$
                    $if(address2)$
                    <addressPart type="addressLine">$address2$</addressPart>
                    $endif$
                    $if(city)$
                    <addressPart type="suburbOrPlaceOrLocality">$city$</addressPart>
                    $endif$
                    $if(state)$
                    <addressPart type="stateOrTerritory">$state$</addressPart>
                    $endif$
                    $if(postalCode)$
                    <addressPart type="postCode">$postalCode$</addressPart>
                    $endif$
                    $if(country)$
                    <addressPart type="country">$country$</addressPart>
                    $endif$
                </physical>
            </address>
        </location>
        $endif$

        <!-- Sydney Uni -->
        <relatedObject>
            <key>$sydneyUniKey$</key>
            <relation type="isPartOf"/>
        </relatedObject>

        <!-- Research Group -->
        <relatedObject>
            <key>$researchDataset.researchProject.researchGroup.keyForRifCs$</key>
            <relation type="isManagedBy"/>
        </relatedObject>
        
        <!-- PI -->
        <relatedObject>
            <key>$researchDataset.researchProject.researchGroup.principalInvestigator.keyForRifCs$</key>
            <relation type="isManagedBy"/>
        </relatedObject>
        
        <!-- Research Project -->
        <relatedObject>
            <key>$researchDataset.researchProject.keyForRifCs$</key>
            <relation type="isOwnedBy"/>
        </relatedObject>
        
        <subject type="anzsrc-for">$researchDataset.subjectCode.subjectCode$</subject>
        $if(hasSubjectCode2)$
		<subject type="anzsrc-for">$researchDataset.subjectCode2.subjectCode$</subject>
		$endif$
		$if(hasSubjectCode3)$
		<subject type="anzsrc-for">$researchDataset.subjectCode3.subjectCode$</subject>
		$endif$

        <description type="brief">$researchDataset.description$</description>
        <description type="rights">$researchDataset.publicAccessRight.description$</description>
    </collection>
</registryObject>
</registryObjects>
