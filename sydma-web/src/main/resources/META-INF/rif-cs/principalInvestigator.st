<?xml version="1.0" encoding="UTF-8"?>
<registryObjects xmlns="http://ands.org.au/standards/rif-cs/registryObjects"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://ands.org.au/standards/rif-cs/registryObjects http://services.ands.org.au/home/orca/schemata/registryObjects.xsd">
<registryObject group="The University of Sydney">
    <key>$user.keyForRifCs$</key>
    <originatingSource>$researchGroup.originatingSource$</originatingSource>

    <party type="person">
        <identifier type="local">person-$user.id$</identifier>

        <name type="primary">
            <namePart type="family">$user.surname$</namePart>
            <namePart type="given">$user.givenname$</namePart>
        </name>
        
        <!-- Sydney Uni -->
        <relatedObject>
            <key>$sydneyUniKey$</key>
            <relation type="isPartOf"/>
        </relatedObject>

        <!-- Research Group -->
        $user.researchGroups:{ group |      
        $if(group.hasAdvertisedProjects)$ 
	        <relatedObject>
	            <key>$group.keyForRifCs$</key>
	            <relation type="isManagerOf"/>
	        </relatedObject>
        
	        <!-- Advertised Projects -->
	        $group.advertisedResearchProjects:{ project | 
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
        <subject type="anzsrc-for">$group.subjectCode.subjectCode$</subject>
        $if(hasSubjectCode2)$
		<subject type="anzsrc-for">$group.subjectCode2.subjectCode$</subject>
		$endif$
		$if(hasSubjectCode3)$
		<subject type="anzsrc-for">$group.subjectCode3.subjectCode$</subject>
		$endif$
		
        $endif$
        }$
    </party>
</registryObject>
</registryObjects>
