package DS;
import java.util.LinkedList;
import java.util.List;

public class test {

	public static void main(String[] args) {

		String clientDoc = "a";
		String clientShadow = "";

		String serverDoc = "";
		String serverShadow = "";

		diff_match_patch dmp = new diff_match_patch();

		LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(clientShadow, clientDoc);

		System.out.println(diff);

		LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);

		System.out.println(patches);

		String patch = dmp.patch_toText(patches);

		System.out.println(patch);

		List<diff_match_patch.Patch> patches2 = dmp.patch_fromText(patch);

		Object[] returnVal = dmp.patch_apply((LinkedList<diff_match_patch.Patch>) patches2, serverDoc);

		System.out.println(returnVal[0].toString());

	}

}
