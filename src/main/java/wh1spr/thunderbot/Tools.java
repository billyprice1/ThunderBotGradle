package wh1spr.thunderbot;

public final class Tools {
	
	public static boolean startsWith(String check, String... toCheck) {
		for (int i = 0; i < toCheck.length; i++) {
			if (check.startsWith(toCheck[i])) return true;
		}
		
		return false;
	}
}
