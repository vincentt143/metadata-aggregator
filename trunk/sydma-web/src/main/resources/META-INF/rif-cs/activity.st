<?xml version="1.0" encoding="UTF-8"?>
<registryObjects xmlns="http://ands.org.au/standards/rif-cs/registryObjects"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://ands.org.au/standards/rif-cs/registryObjects http://services.ands.org.au/documentation/rifcs/1.3/schema/registryObjects.xsd">
<registryObject group="$researchProject.researchGroup.name$">
    <key>$researchProject.keyForRifCs$</key>
    <originatingSource>$researchProject.originatingSource$</originatingSource>
    <activity type="project">
        <identifier type="local">project-$researchProject.id$</identifier>

        <name type="primary">
            <namePart>$researchProject.name$</namePart>
        </name>
        
        $if(url)$
        <location>
            <address>
                <electronic type="url">
                    <value>$url$</value>
                </electronic>
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
            <key>$researchProject.researchGroup.keyForRifCs$</key>
            <relation type="isManagedBy"/>
        </relatedObject>

        <!-- PI -->
        <relatedObject>
            <key>$researchProject.researchGroup.principalInvestigator.keyForRifCs$</key>
            <relation type="isManagedBy"/>
        </relatedObject>
        
        <!-- Advertised Datasets -->
        $researchProject.advertisedResearchDatasets:{ dataset |
        <relatedObject>
            <key>$dataset.keyForRifCs$</key>
            <relation type="isOwnerOf"/>
        </relatedObject>
        }$

        <subject type="anzsrc-for">$researchProject.subjectCode.subjectCode$</subject>
        $if(hasSubjectCode2)$
		<subject type="anzsrc-for">$researchProject.subjectCode2.subjectCode$</subject>
		$endif$
		$if(hasSubjectCode3)$
		<subject type="anzsrc-for">$researchProject.subjectCode3.subjectCode$</subject>
		$endif$
        <description type="brief">$researchProject.description$</description>
    </activity>
</registryObject>
</registryObjects>
