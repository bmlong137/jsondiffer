package org.alfresco.support.jsondiffer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Differ<T, PathElementType> {

	/**
	 * Compute a list of patches required to produce target from source.  This
	 * means an Add patch will happen when the source has something the target
	 * does not.  Likewise, a Remove patch will happen when the target has
	 * something the source does not.  A Replace patch will happen when both
	 * have the same path but different values.  The value of the patch will
	 * be the target, not source, value.  Applying these patches to the source
	 * will result in the target.  The reverse is not true.
	 * 
	 * @param source A data structure
	 * @param target A target or resultant data structure
	 * @return A collection of patches
	 */
	List<Patch<PathElementType>> diff(T source, T target);
	
	/**
	 * Compute the target data structure based on the source data structure and
	 * some patches.
	 *  
	 * @param source A data structure
	 * @param patches A collection of patches
	 * @return A data structure
	 */
	T patch(T source, Collection<Patch<PathElementType>> patches);
	
	/**
	 * A utility method to remove ignored patches.  This is particularly useful
	 * when you want to merge two data structures instead of finding all the
	 * differences.  In this case, you could just trim the Remove operations.
	 * 
	 * @param patches A collection of patches
	 * @param exclusionPatchFilters A set of patch filters to remove
	 * @return A collection of patches
	 */
	List<Patch<PathElementType>> trim(Collection<Patch<PathElementType>> patches, Set<PatchFilter<PathElementType>> exclusionPatchFilters);
	
}
