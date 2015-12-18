package csu.common.test.escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rescuecore2.worldmodel.EntityID;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;

public class EscapeData_3 implements EscapeData {

	private int[][] road_16888 = { { 1153951, 584907, 1150722, 601746, 0 },
			{ 1150722, 601746, 1144967, 599896, 16897 },
			{ 1144967, 599896, 1148004, 584059, 0 },
			{ 1148004, 584059, 1153951, 584907, 16879 } };

	private int[][] road_16879 = { { 1155620, 567112, 1153951, 584907, 0 },
			{ 1153951, 584907, 1148004, 584059, 16888 },
			{ 1148004, 584059, 1149374, 569457, 0 },
			{ 1149374, 569457, 1155620, 567112, 3384 } };

	private int[][] road_16870 = { { 1157375, 546871, 1155620, 567112, 0 },
			{ 1155620, 567112, 1149841, 563465, 3384 },
			{ 1149841, 563465, 1151374, 539471, 0 },
			{ 1151374, 539471, 1157375, 546871, 4637 } };

	private int[][] road_16897 = { { 1150722, 601746, 1147426, 608879, 0 },
			{ 1147426, 608879, 1142476, 605289, 5826 },
			{ 1142476, 605289, 1144967, 599896, 0 },
			{ 1144967, 599896, 1150722, 601746, 16888 } };

	private int[][] road_17014 = { { 1147426, 608879, 1197695, 619591, 0 },
			{ 1197695, 619591, 1203945, 629084, 3759 },
			{ 1203945, 629084, 1143639, 614122, 0 },
			{ 1143639, 614122, 1147426, 608879, 5826 } };

	private int[][] road_3384 = { { 1149374, 569457, 1149841, 563465, 12838 },
			{ 1149841, 563465, 1155620, 567112, 16870 },
			{ 1155620, 567112, 1149374, 569457, 16879 } };

	private int[][] road_3759 = { { 1203945, 629084, 1197695, 619591, 17014 },
			{ 1197695, 619591, 1205075, 615022, 12334 },
			{ 1205075, 615022, 1210244, 627246, 21955 },
			{ 1210244, 627246, 1203945, 629084, 18913 } };

	private int[][] road_4637 = { { 1151374, 539471, 1152061, 530497, 21496 },
			{ 1152061, 530497, 1157727, 534510, 17842 },
			{ 1157727, 534510, 1157375, 546871, 12343 },
			{ 1157375, 546871, 1151374, 539471, 16870 } };

	private int[][] road_5826 = { { 1138643, 610088, 1142476, 605289, 9076 },
			{ 1142476, 605289, 1147426, 608879, 16897 },
			{ 1147426, 608879, 1143639, 614122, 17014 },
			{ 1143639, 614122, 1138643, 610088, 16834 } };

	private int[][] road_7584 = { { 1172603, 572465, 1176451, 567117, 12343 },
			{ 1176451, 567117, 1177698, 569288, 48839 },
			{ 1177698, 569288, 1172603, 572465, 12334 } };

	private int[][] road_12334 = { { 1197695, 619591, 1187453, 600355, 0 },
			{ 1187453, 600355, 1172603, 572465, 52243 },
			{ 1172603, 572465, 1177698, 569288, 7584 },
			{ 1177698, 569288, 1205075, 615022, 0 },
			{ 1205075, 615022, 1197695, 619591, 3759 } };

	private int[][] road_12343 = { { 1172603, 572465, 1157375, 546871, 0 },
			{ 1157375, 546871, 1157727, 534510, 4637 },
			{ 1157727, 534510, 1176451, 567117, 0 },
			{ 1176451, 567117, 1172603, 572465, 7584 } };

	private int[] blockade_2144890562 = { 1174515, 569806, 1172603, 572464,
			1175338, 570759, 1174152, 571009 };
	private int[] blockade_2144890801 = { 1152642, 565232, 1152400, 566122,
			1152316, 567068, 1152459, 568298, 1155620, 567112, 1153485, 566868 };
	private int[] blockade_2144890869 = { 1201608, 617167, 1197695, 619591,
			1199644, 622552, 1200507, 621725, 1201167, 620723, 1201588, 619580,
			1201736, 618331, 1200005, 619826 };
	private int[] blockade_2144889182 = { 1170940, 568420, 1170224, 568468,
			1172603, 572464, 1174515, 569806, 1173752, 569226, 1172890, 568790,
			1171947, 568515, 1172443, 570023 };
	private int[] blockade_2144889181 = { 1158557, 546764, 1157388, 546893,
			1163547, 557245, 1163847, 552447, 1163860, 552068, 1163752, 550999,
			1163443, 550003, 1162954, 549102, 1162307, 548317, 1161522, 547670,
			1160621, 547181, 1159625, 546872, 1161416, 550810 };
	private int[] blockade_2144889172 = { 1193797, 612270, 1197695, 619591,
			1201608, 617167, 1201066, 615748, 1200161, 614558, 1198960, 613667,
			1197533, 613141, 1197918, 615743 };
	private int[] blockade_2144889171 = { 1186416, 595500, 1184973, 595698,
			1187453, 600355, 1189846, 604849, 1190622, 604034, 1191212, 603069,
			1191588, 601983, 1191719, 600804, 1191611, 599735, 1191302, 598739,
			1190813, 597838, 1190166, 597053, 1189381, 596406, 1188480, 595917,
			1187484, 595608, 1188944, 599458 };
	private int[] blockade_2144889170 = { 1175338, 570759, 1172603, 572465,
			1175055, 577070, 1175554, 576340, 1175928, 575530, 1176162, 574653,
			1176243, 573724, 1176005, 572147, 1174764, 573612 };
	private int[] blockade_2144889134 = { 1151520, 537558, 1151374, 539471,
			1154076, 542804, 1154089, 542415, 1153906, 540953, 1153386, 539626,
			1152576, 538479, 1152701, 540000 };
	private int[] blockade_2144889471 = { 1155619, 567112, 1152459, 568298,
			1152847, 569383, 1153449, 570345, 1154237, 571153, 1155182, 571779,
			1155620, 567112, 1154302, 569235 };
	private int[] blockade_2144889459 = { 1150716, 584445, 1150597, 585568,
			1150767, 586905, 1151249, 588117, 1151999, 589160, 1152975, 589992,
			1153951, 584907, 1152236, 586718 };
	private int[] blockade_2144889460 = { 1151561, 597370, 1150267, 597813,
			1149149, 598557, 1148255, 599553, 1147634, 600753, 1150722, 601746,
			1149844, 599699 };
	private int[] blockade_2144889479 = { 1156063, 561996, 1154941, 562489,
			1153972, 563217, 1153194, 564143, 1152642, 565232, 1155620, 567112,
			1154679, 564557 };
	private int[] blockade_2144889478 = { 1157372, 546897, 1156518, 547170,
			1155733, 547577, 1154428, 548738, 1153565, 550271, 1153333, 551143,
			1153253, 552068, 1153448, 553498, 1153999, 554781, 1156334, 558866,
			1155449, 552177 };
	private int[] blockade_2144889472 = { 1154365, 580490, 1153080, 581075,
			1152007, 581966, 1151200, 583108, 1150716, 584445, 1153951, 584907,
			1152837, 583023 };
	private int[] blockade_2144889481 = { 1150593, 551682, 1149950, 561750,
			1151248, 560842, 1152256, 559625, 1152908, 558164, 1153140, 556524,
			1152959, 555068, 1152444, 553747, 1151640, 552603, 1151428, 556783 };
	private int[] blockade_2144889480 = { 1151374, 539471, 1150850, 547666,
			1152099, 546821, 1153090, 545692, 1153768, 544334, 1154076, 542804,
			1152218, 543716 };
	private int[] blockade_2144889675 = { 1147426, 608879, 1146054, 610778,
			1146880, 611642, 1147882, 612302, 1149025, 612723, 1150276, 612871,
			1151630, 612697, 1152854, 612203, 1153905, 611435, 1154737, 610437,
			1150047, 610966 };
	private int[] blockade_2144889677 = { 1190122, 617977, 1191777, 620873,
			1192602, 621998, 1193689, 622871, 1194985, 623434, 1196433, 623634,
			1198157, 623348, 1199644, 622552, 1197695, 619591, 1195060, 620988 };
	private int[] blockade_2144889652 = { 1147634, 600753, 1147398, 601634,
			1147316, 602568, 1147347, 603145, 1146445, 603899, 1145729, 604834,
			1145234, 605916, 1144991, 607113, 1147426, 608879, 1150722, 601746,
			1147733, 604685 };
	private int[] blockade_2144889802 = { 1144991, 607113, 1144972, 607568,
			1145258, 609291, 1146054, 610778, 1147426, 608879, 1146023, 608927 };

	int[] raod_16888_blockade = { 2144889459, 2144889460 };
	int[] raod_16879_blockade = { 2144889471, 2144889472 };
	int[] raod_16870_blockade = { 2144889478, 2144889479, 2144889480,
			2144889481 };
	int[] raod_16897_blockade = { 2144889652 };
	int[] raod_17014_blockade = { 2144889675, 2144889677 };
	int[] raod_3384_blockade = { 2144890801 };
	int[] raod_3759_blockade = { 2144890869 };
	int[] raod_4637_blockade = { 2144889134 };
	int[] raod_5826_blockade = { 2144889802 };
	int[] raod_7584_blockade = { 2144890562 };
	int[] raod_12334_blockade = { 2144889170, 2144889171, 2144889172 };
	int[] raod_12343_blockade = { 2144889181, 2144889182 };

	int[] road = { 5826, 17014, 3759, 12334, 7584, 12343, 4637, 16870, 3384,
			16879, 16888, 16897 };

	int[] blockade = { 2144889459, 2144889460, 2144889471, 2144889472,
			2144889478, 2144889479, 2144889480, 2144889481, 2144889652,
			2144889675, 2144889677, 2144890801, 2144890869, 2144889134,
			2144889802, 2144890562, 2144889170, 2144889171, 2144889172,
			2144889181, 2144889182 };

	@Override
	public List<CSURoad> roadList() {
		List<CSURoad> list = new ArrayList<>();

		list.add(createRoadEdge(5826, road_5826));
		list.add(createRoadEdge(17014, road_17014));
		list.add(createRoadEdge(3759, road_3759));
		list.add(createRoadEdge(12334, road_12334));
		list.add(createRoadEdge(7584, road_7584));
		list.add(createRoadEdge(12343, road_12343));
		list.add(createRoadEdge(4637, road_4637));
		list.add(createRoadEdge(16870, road_16870));
		list.add(createRoadEdge(3384, road_3384));
		list.add(createRoadEdge(16879, road_16879));
		list.add(createRoadEdge(16888, road_16888));
		list.add(createRoadEdge(16897, road_16897));

		return list;
	}

	@Override
	public Map<Integer, CSUBlockade> blockadeList() {
		Map<Integer, CSUBlockade> list = new TreeMap<>();

		list.put(2144889459, createBlockade(2144889459, blockade_2144889459));
		list.put(2144889460, createBlockade(2144889460, blockade_2144889460));
		list.put(2144889471, createBlockade(2144889471, blockade_2144889471));
		list.put(2144889472, createBlockade(2144889472, blockade_2144889472));
		list.put(2144889478, createBlockade(2144889478, blockade_2144889478));
		list.put(2144889479, createBlockade(2144889479, blockade_2144889479));
		list.put(2144889480, createBlockade(2144889480, blockade_2144889480));
		list.put(2144889481, createBlockade(2144889481, blockade_2144889481));
		list.put(2144889652, createBlockade(2144889652, blockade_2144889652));
		list.put(2144889675, createBlockade(2144889675, blockade_2144889675));
		list.put(2144889677, createBlockade(2144889677, blockade_2144889677));
		list.put(2144890801, createBlockade(2144890801, blockade_2144890801));
		list.put(2144890869, createBlockade(2144890869, blockade_2144890869));
		list.put(2144889134, createBlockade(2144889134, blockade_2144889134));
		list.put(2144889802, createBlockade(2144889802, blockade_2144889802));
		list.put(2144890562, createBlockade(2144890562, blockade_2144890562));
		list.put(2144889170, createBlockade(2144889170, blockade_2144889170));
		list.put(2144889171, createBlockade(2144889171, blockade_2144889171));
		list.put(2144889172, createBlockade(2144889172, blockade_2144889172));
		list.put(2144889181, createBlockade(2144889181, blockade_2144889181));
		list.put(2144889182, createBlockade(2144889182, blockade_2144889182));

		return list;
	}

	@Override
	public int[] getBlockadeList(int roadId) {
		switch (roadId) {
		case 5826:
			return raod_5826_blockade;
		case 17014:
			return raod_17014_blockade;
		case 3759:
			return raod_3759_blockade;
		case 12334:
			return raod_12334_blockade;
		case 7584:
			return raod_7584_blockade;
		case 12343:
			return raod_12343_blockade;
		case 4637:
			return raod_4637_blockade;
		case 16870:
			return raod_16870_blockade;
		case 3384:
			return raod_3384_blockade;
		case 16879:
			return raod_16879_blockade;
		case 16888:
			return raod_16888_blockade;
		case 16897:
			return raod_16897_blockade;
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

		EscapeData_3 es = new EscapeData_3();
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
