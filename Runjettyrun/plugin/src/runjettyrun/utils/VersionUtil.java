package runjettyrun.utils;

public class VersionUtil {
	public static final boolean supportVersion(String version, String supportVersion, String... supportVersionPrefix){
		if(version.equalsIgnoreCase(supportVersion)){
			return true;
		}
		String lv = version.toLowerCase();
		for(String svp: supportVersionPrefix){
			if(lv.startsWith(svp.toLowerCase())){
				return true;
			}
		}
		return false;
	}
}
