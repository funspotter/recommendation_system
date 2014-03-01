package com.incredibles.data;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**Class to handle Imdbtag and Lastfmtag table data*/
public class MetadataFromThirdParty {
	
	protected String thirdPartyId;
	protected List<String> tagArray;
	protected long ourLikeId;
	
	public MetadataFromThirdParty(Long ourLikeId, String thirdPartyId, List<String> tagArray){
		this.ourLikeId = ourLikeId;
		this.tagArray = tagArray;
		this.thirdPartyId = thirdPartyId;
	}

	public String getThirdPartyId() {
		return thirdPartyId;
	}

	public void setThirdPartyId(String thirdPartyId) {
		this.thirdPartyId = thirdPartyId;
	}

	public List<String> getTagArray() {
		return tagArray;
	}

	public void setTagArray(List<String> tagJson) {
		this.tagArray = tagJson;
	}

	public long getOurLikeId() {
		return ourLikeId;
	}

	public void setOurLikeId(long ourLikeId) {
		this.ourLikeId = ourLikeId;
	}
		
	public String getJsonString(){
		List<String> TagArray = this.tagArray;
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		for(int i=0; i<TagArray.size(); i++){
			list.add(TagArray.get(i));
		}
		obj.put("tags", list);
		String jsonString = obj.toString();
		return jsonString;
	}
	
	
}
