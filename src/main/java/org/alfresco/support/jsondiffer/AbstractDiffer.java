package org.alfresco.support.jsondiffer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AbstractDiffer<T, PathElementType> implements Differ<T, PathElementType> {
	
	@Override
	public List<Patch<PathElementType>> trim(Collection<Patch<PathElementType>> patches, Set<PatchFilter<PathElementType>> exclusionPatchFilters) {
		List<Patch<PathElementType>> trimmedPatches = new LinkedList<Patch<PathElementType>>(patches);
		
		for (PatchFilter<PathElementType> patchFilter : exclusionPatchFilters)
			patchFilter.doExclude(trimmedPatches);
		
		return trimmedPatches;
	}

}
