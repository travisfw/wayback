/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcWarcFilenameFilter;


/**
 * A ResourceStore which contains one or more other resource stores.
 * This container simply searches each one in turn for a requested
 * resource, returning the first one found.
 */
public class MultipleResourceStore implements ResourceStore {

	private final static Logger LOGGER = Logger.getLogger(MultipleResourceStore.class.getName());

	private List<ResourceStore> stores;

	public Resource retrieveResource(CaptureSearchResult result)
		throws ResourceNotAvailableException {
    
		String fileName = result.getFile();
		if(fileName == null || fileName.length() < 1) {
			throw new ResourceNotAvailableException("No ARC/WARC name in search result...");
		}

		final long offset = result.getOffset();
		if(!fileName.endsWith(ArcWarcFilenameFilter.ARC_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.ARC_GZ_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.WARC_SUFFIX)
				&& !fileName.endsWith(ArcWarcFilenameFilter.WARC_GZ_SUFFIX)) {
			fileName = fileName + ArcWarcFilenameFilter.ARC_GZ_SUFFIX;
		}

		for ( ResourceStore store : stores ) {
			try {
				Resource r = store.retrieveResource(result);

				if ( r != null ) return r;
			} catch (ResourceNotAvailableException e) {
				LOGGER.warning(e.toString());
			}
		}

		throw new ResourceNotAvailableException("Unable to retrieve: "+fileName);
	}

	public void setStores(List<ResourceStore> stores) {
		this.stores = new ArrayList<ResourceStore>(stores);
	}
  
	public List<ResourceStore> getStores( ) {
		return new ArrayList<ResourceStore>(this.stores);
	}

	public void shutdown() throws IOException {
		// no-op
	}
}
