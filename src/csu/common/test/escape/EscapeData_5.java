package csu.common.test.escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rescuecore2.worldmodel.EntityID;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;

public class EscapeData_5 implements EscapeData {
	private int[][] road_281 = { { 808926, 615229, 809789, 605266, 15736 },
			{ 809789, 605266, 815772, 605735, 12127 },
			{ 815772, 605735, 814908, 615699, 14044 },
			{ 814908, 615699, 808926, 615229, 12118 } };

	private int[][] road_320 = { { 719725, 597176, 722231, 597401, 32867 },
			{ 722231, 597401, 720641, 607299, 15682 },
			{ 720641, 607299, 719725, 597176, 15709 } };

	private int[][] road_4342 = { { 771254, 611846, 771503, 601828, 15691 },
			{ 771503, 601828, 774001, 602052, 42967 },
			{ 774001, 602052, 773747, 612069, 15736 },
			{ 773747, 612069, 771254, 611846, 42211 } };

	private int[][] road_5181 = { { 731904, 608311, 734339, 598489, 15682 },
			{ 734339, 598489, 734398, 608535, 15691 },
			{ 734398, 608535, 731904, 608311, 32774 } };

	private int[][] road_7559 = { { 839743, 617371, 841446, 607463, 14044 },
			{ 841446, 607463, 843944, 607631, 36784 },
			{ 843944, 607631, 842264, 617541, 14053 },
			{ 842264, 617541, 839743, 617371, 44127 } };

	private int[][] road_12118 = { { 814908, 615699, 808206, 702657, 0 },
			{ 808206, 702657, 802224, 702192, 2536 },
			{ 802224, 702192, 808926, 615229, 0 },
			{ 808926, 615229, 814908, 615699, 281 } };

	private int[][] road_14044 = { { 815772, 605735, 841446, 607463, 0 },
			{ 841446, 607463, 839743, 617371, 7559 },
			{ 839743, 617371, 814908, 615699, 0 },
			{ 814908, 615699, 815772, 605735, 281 } };

	private int[][] road_15736 = { { 808926, 615229, 773747, 612069, 0 },
			{ 773747, 612069, 774001, 602052, 4342 },
			{ 774001, 602052, 809789, 605266, 0 },
			{ 809789, 605266, 808926, 615229, 281 } };

	private int[][] road_15691 = { { 771254, 611846, 734398, 608535, 0 },
			{ 734398, 608535, 734339, 598489, 5181 },
			{ 734339, 598489, 771503, 601828, 0 },
			{ 771503, 601828, 771254, 611846, 4342 } };

	private int[][] road_15682 = { { 731904, 608311, 720641, 607299, 0 },
			{ 720641, 607299, 722231, 597401, 320 },
			{ 722231, 597401, 734339, 598489, 0 },
			{ 734339, 598489, 731904, 608311, 5181 } };

	private int[][] road_15709 = { { 720641, 607299, 687289, 604303, 0 },
			{ 687289, 604303, 688069, 594333, 5167 },
			{ 688069, 594333, 719725, 597176, 0 },
			{ 719725, 597176, 720641, 607299, 320 } };

	private int[] blockade_2144890959 = { 722231, 597401, 721648, 601028,
			722159, 601046, 723912, 600822, 725500, 600188, 726866, 599200,
			727952, 597915, 724238, 599079 };
	private int[] blockade_2144890958 = { 727080, 605125, 725575, 605284,
			724180, 605739, 722929, 606458, 721855, 607408, 731904, 608311,
			732569, 605628, 731258, 605508, 729790, 605659, 728482, 605263,
			727820, 606698 };
	private int[] blockade_2144890953 = { 759210, 608069, 757723, 608224,
			756344, 608669, 755104, 609371, 754034, 610299, 765174, 611299,
			764057, 609974, 762652, 608954, 761016, 608300, 759690, 609710 };
	private int[] blockade_2144890952 = { 734381, 605763, 734398, 608535,
			739748, 609015, 738737, 607773, 737475, 606786, 736008, 606100,
			736465, 607596 };
	private int[] blockade_2144890955 = { 759210, 604922, 757822, 605015,
			756491, 605286, 755229, 605723, 754047, 606313, 752958, 607045,
			751973, 607906, 751106, 608885, 750367, 609969, 771254, 611846,
			771395, 606154, 770894, 606142, 769564, 606228, 768285, 606477,
			767069, 606879, 765925, 607423, 764477, 606375, 762855, 605588,
			761089, 605094, 761821, 608471 };
	private int[] blockade_2144890954 = { 743366, 599300, 744420, 600930,
			745876, 602201, 747650, 603028, 748629, 603247, 749655, 603322,
			751353, 603112, 752899, 602517, 754239, 601587, 755324, 600374,
			749229, 601243 };
	private int[] blockade_2144890964 = { 688882, 594406, 689327, 595338,
			689900, 596188, 690590, 596942, 691384, 597587, 692268, 598110,
			693231, 598500, 694259, 598743, 695340, 598827, 697146, 598589,
			698774, 597916, 700162, 596871, 701247, 595516, 694904, 596528 };
	private int[] blockade_2144890965 = { 713267, 596596, 714302, 598310,
			715775, 599652, 717590, 600527, 718596, 600759, 719653, 600839,
			720055, 600828, 719725, 597176, 717232, 598543 };
	private int[] blockade_2144890963 = { 694604, 602155, 693082, 602317,
			691674, 602783, 690413, 603518, 689334, 604486, 700643, 605502,
			699527, 604131, 698106, 603074, 696445, 602395, 695079, 603860 };
	private int[] blockade_2144890943 = { 773890, 606426, 773747, 612069,
			808926, 615229, 809277, 611171, 808034, 610498, 806694, 609999,
			805274, 609689, 803789, 609582, 801974, 609742, 800264, 610203,
			799030, 609544, 797703, 609055, 796297, 608751, 794827, 608646,
			793516, 608729, 792254, 608971, 791053, 609362, 789921, 609892,
			788481, 608862, 786871, 608089, 785119, 607604, 783258, 607435,
			781237, 607634, 779350, 608205, 778121, 607485, 776792, 606938,
			775376, 606580, 791195, 611127 };
	private int[] blockade_2144890589 = { 840459, 613201, 839743, 617371,
			842264, 617541, 842963, 613417, 840984, 613225, 840607, 613232,
			841355, 615379 };
	private int[] blockade_2144890812 = { 803215, 689323, 802224, 702192,
			803990, 702329, 804815, 700989, 805431, 699523, 805815, 697953,
			805948, 696298, 805759, 694329, 805217, 692487, 804358, 690807,
			804034, 696558 };
	private int[] blockade_2144890808 = { 814710, 618264, 814191, 620064,
			814011, 621984, 814252, 624200, 814319, 621185 };
	private int[] blockade_2144890809 = { 812240, 650308, 811760, 652046,
			811593, 653893, 811804, 655967, 811875, 653100 };
	private int[] blockade_2144890810 = { 808926, 615229, 806704, 644057,
			807931, 643348, 809044, 642481, 810027, 641473, 810865, 640337,
			811543, 639090, 812046, 637746, 812359, 636321, 812467, 634829,
			812293, 632936, 811791, 631158, 810994, 629527, 809932, 628074,
			811632, 626472, 812932, 624522, 813762, 622291, 814055, 619849,
			813812, 617618, 813118, 615558, 810151, 628605 };
	private int[] blockade_2144890811 = { 804711, 669917, 803481, 685877,
			805318, 684267, 806728, 682266, 807248, 681143, 807632, 679951,
			807869, 678701, 807951, 677403, 807725, 675251, 807079, 673255,
			806058, 671462, 805644, 678010 };
	private int[] blockade_2144888850 = { 771395, 606154, 771254, 611846,
			773747, 612069, 773890, 606426, 773566, 606421, 773386, 606423,
			772074, 606209, 772567, 609128 };
	private int[] blockade_2144889456 = { 732569, 605628, 731904, 608311,
			734398, 608535, 734381, 605763, 733752, 605736, 733147, 605761,
			733306, 607147 };
	private int[] blockade_2144889551 = { 824219, 611776, 821820, 612059,
			819626, 612865, 817701, 614127, 816112, 615780, 839743, 617371,
			840459, 613201, 838472, 613008, 836662, 613168, 834958, 613629,
			833385, 614364, 831972, 615344, 830407, 613865, 828557, 612741,
			826476, 612026, 828772, 614758 };
	private int[] blockade_2144888705 = { 719725, 597176, 720055, 600828,
			720342, 600805, 721648, 601028, 722231, 597401, 720924, 598961 };
	private int[] blockade_2144888663 = { 809277, 611171, 808926, 615229,
			813118, 615558, 812401, 614259, 811510, 613083, 810463, 612048,
			810574, 613875 };

	int[] raod_281_blockade = { 2144888663 };
	int[] raod_320_blockade = { 2144888705 };
	int[] raod_4342_blockade = { 2144888850 };
	int[] raod_5181_blockade = { 2144889456 };
	int[] raod_7559_blockade = { 2144890589 };
	int[] raod_12118_blockade = { 2144890808, 2144890809, 2144890810,
			2144890811, 2144890812 };
	int[] raod_14044_blockade = { 2144889551 };
	int[] raod_15736_blockade = { 2144890943 };
	int[] raod_15691_blockade = { 2144890952, 2144890953, 2144890954,
			2144890955 };
	int[] raod_15682_blockade = { 2144890958, 2144890959 };
	int[] raod_15709_blockade = { 2144890963, 2144890964, 2144890965 };

	int[] road = { 15709, 320, 15682, 5181, 15691, 4342, 15736, 281, 14044,
			7559, 4053, 12118 };

	int[] blockade = { 2144888663, 2144888705, 2144888850, 2144889456,
			2144890589, 2144890808, 2144890809, 2144890810, 2144890811,
			2144890812, 2144889551, 2144890943, 2144890952, 2144890953,
			2144890954, 2144890955, 2144890958, 2144890959, 2144890963,
			2144890964, 2144890965 };

	@Override
	public List<CSURoad> roadList() {
		List<CSURoad> list = new ArrayList<>();

		list.add(createRoadEdge(15709, road_15709));
		list.add(createRoadEdge(320, road_320));
		list.add(createRoadEdge(15682, road_15682));
		list.add(createRoadEdge(5181, road_5181));
		list.add(createRoadEdge(15691, road_15691));
		list.add(createRoadEdge(4342, road_4342));
		list.add(createRoadEdge(15736, road_15736));
		list.add(createRoadEdge(281, road_281));
		list.add(createRoadEdge(14044, road_14044));
		list.add(createRoadEdge(7559, road_7559));
		list.add(createRoadEdge(12118, road_12118));

		return list;
	}

	@Override
	public Map<Integer, CSUBlockade> blockadeList() {
		Map<Integer, CSUBlockade> list = new TreeMap<>();

		list.put(2144888663, createBlockade(2144888663, blockade_2144888663));
		list.put(2144888705, createBlockade(2144888705, blockade_2144888705));
		list.put(2144888850, createBlockade(2144888850, blockade_2144888850));
		list.put(2144889456, createBlockade(2144889456, blockade_2144889456));
		list.put(2144890589, createBlockade(2144890589, blockade_2144890589));
		list.put(2144890808, createBlockade(2144890808, blockade_2144890808));
		list.put(2144890809, createBlockade(2144890809, blockade_2144890809));
		list.put(2144890810, createBlockade(2144890810, blockade_2144890810));
		list.put(2144890811, createBlockade(2144890811, blockade_2144890811));
		list.put(2144890812, createBlockade(2144890812, blockade_2144890812));
		list.put(2144889551, createBlockade(2144889551, blockade_2144889551));
		list.put(2144890943, createBlockade(2144890943, blockade_2144890943));
		list.put(2144890952, createBlockade(2144890952, blockade_2144890952));
		list.put(2144890953, createBlockade(2144890953, blockade_2144890953));
		list.put(2144890954, createBlockade(2144890954, blockade_2144890954));
		list.put(2144890955, createBlockade(2144890955, blockade_2144890955));
		list.put(2144890958, createBlockade(2144890958, blockade_2144890958));
		list.put(2144890959, createBlockade(2144890959, blockade_2144890959));
		list.put(2144890963, createBlockade(2144890963, blockade_2144890963));
		list.put(2144890964, createBlockade(2144890964, blockade_2144890964));
		list.put(2144890965, createBlockade(2144890965, blockade_2144890965));

		return list;
	}

	@Override
	public int[] getBlockadeList(int roadId) {
		switch (roadId) {
		case 15709:
			return raod_15709_blockade;
		case 320:
			return raod_320_blockade;
		case 15682:
			return raod_15682_blockade;
		case 5181:
			return raod_5181_blockade;
		case 15691:
			return raod_15691_blockade;
		case 4342:
			return raod_4342_blockade;
		case 15736:
			return raod_15736_blockade;
		case 281:
			return raod_281_blockade;
		case 14044:
			return raod_14044_blockade;
		case 7559:
			return raod_7559_blockade;
		case 4053:
			int[] a = {};
			return a;
		case 12118:
			return raod_12118_blockade;
		default:
			return null;
		}
	}

	private CSUBlockade createBlockade(int id, int[] apexes) {
		EntityID blockadeId = new EntityID(id);

		int count = apexes.length;
		int[] vertexs = new int[count - 2];
		for (int i = 0; i < vertexs.length; i++) {
			vertexs[i] = apexes[i];
		}

		int center_x = apexes[count - 2], center_y = apexes[count - 1];

		return new CSUBlockade(blockadeId, vertexs, center_x, center_y);
	}

	private CSURoad createRoadEdge(int id, int[][] road) {
		List<CSUEdge> edges = new ArrayList<>();
		rescuecore2.misc.geometry.Point2D start = null, end = null;

		for (int i = 0; i < road.length; i++) {
			start = new rescuecore2.misc.geometry.Point2D(road[i][0],
					road[i][1]);
			end = new rescuecore2.misc.geometry.Point2D(road[i][2], road[i][3]);

			edges.add(new CSUEdge(start, end, road[i][4] != 0));
		}

		CSURoad csuRoad = new CSURoad(new EntityID(id), edges);

		return csuRoad;
	}

	public static void main(String[] args) {

		EscapeData_5 es = new EscapeData_5();
		es.print_road();
		System.out.println();
		es.print_blockade();
		System.out.println();
		es.printAssignBlockade();
	}

	public void print_road() {
		String road = "@Override\npublic List<CSURoad> roadList() {\n\t"
				+ "List<CSURoad> list = new ArrayList<>();\n\n\t";
		for (int i = 0; i < this.road.length; i++) {
			road = road + "list.add(createRoadEdge(" + this.road[i] + ", road_"
					+ this.road[i] + "));\n\t";
		}
		road = road + "\n\treturn list;\n}";
		System.out.println(road);
	}

	public void print_blockade() {
		String blockade = "@Override\npublic Map<Integer, CSUBlockade> blockadeList() {\n\t"
				+ "Map<Integer, CSUBlockade> list = new TreeMap<>();\n\n\t";
		for (int i = 0; i < this.blockade.length; i++) {
			blockade = blockade + "list.put(" + this.blockade[i]
					+ ", createBlockade(" + this.blockade[i] + ", blockade_"
					+ this.blockade[i] + "));\n\t";
		}
		blockade = blockade + "\n\treturn list;\n}";
		System.out.println(blockade);
	}

	public void printAssignBlockade() {
		String assign = "@Override\npublic int[] getBlockadeList(int roadId) {\n\tswitch (roadId) {\n\t";
		for (int i = 0; i < this.road.length; i++) {
			assign = assign + "case " + this.road[i] + ":\n\t\t";
			assign = assign + "return raod_" + this.road[i] + "_blockade;\n\t";
		}
		assign = assign + "default:\n\t\treturn null;\n\t}\n}";
		System.out.println(assign);
	}

}
