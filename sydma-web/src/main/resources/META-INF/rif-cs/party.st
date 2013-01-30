<?xml version="1.0" encoding="UTF-8"?>
<registryObjects xmlns="http://ands.org.au/standards/rif-cs/registryObjects"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://ands.org.au/standards/rif-cs/registryObjects http://services.ands.org.au/home/orca/schemata/registryObjects.xsd">
<registryObject group="The University of Sydney">
    <key>$researchGroup.keyForRifCs$</key>
    <originatingSource>$researchGroup.originatingSource$</originatingSource>
    <party type="group">
        <identifier type="local">group-$researchGroup.id$</identifier>

        <name type="primary">
            <namePart>$researchGroup.name$</namePart>
        </name>
        
        $if(url)$
        <location>
            <address>
                <electronic type="url">
                    <value>$researchGroup.url$</value>
                </electronic>
            </address>
        </location>
        $endif$

        <!-- Sydney Uni -->
        <relatedObject>
            <key>$sydneyUniKey$</key>
            <relation type="isPartOf"/>
        </relatedObject>

        <!-- PI -->
        <relatedObject>
            <key>$researchGroup.principalInvestigator.keyForRifCs$</key>
            <relation type="isManagedBy"/>
        </relatedObject>
        
        <!-- Advertised Projects -->
        $researchGroup.advertisedResearchProjects:{ project | 
        <relatedObject>
            <key>$project.keyForRifCs$</key>
            <relation type="isOwnerOf"/>
        </relatedObject>
        
        <!-- Advertised Datasets -->
        $project.advertisedResearchDatasets:{ dataset |
        <relatedObject>
            <key>$dataset.keyForRifCs$</key>
            <relation type="isOwnerOf"/>
        </relatedObject>
        }$
        }$

        <subject type="anzsrc-for">$researchGroup.subjectCode.subjectCode$</subject>
		$if(hasSubjectCode2)$
		<subject type="anzsrc-for">$researchGroup.subjectCode2.subjectCode$</subject>
		$endif$
		$if(hasSubjectCode3)$
		<subject type="anzsrc-for">$researchGroup.subjectCode3.subjectCode$</subject>
		$endif$
        $if(description)$
        <description type="brief">$description$</description>
        $endif$

    </party>
</registryObject>
</registryObjects>
