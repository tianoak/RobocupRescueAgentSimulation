package csu.common.test.escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rescuecore2.worldmodel.EntityID;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;

public class EscapeData_2 implements EscapeData {

	private int[][] road_1916 = { { 737052, 1242349, 739542, 1242710, 29741 },
			{ 739542, 1242710, 737632, 1248398, 23233 },
			{ 737632, 1248398, 737052, 1242349, 23242 } };

	private int[][] road_3171 = { { 762633, 1262729, 762346, 1253411, 23215 },
			{ 762346, 1253411, 768990, 1260080, 8734 },
			{ 768990, 1260080, 767921, 1266578, 22900 },
			{ 767921, 1266578, 762633, 1262729, 12451 } };

	private int[][] road_23224 = { { 755848, 1256721, 746933, 1251437, 0 },
			{ 746933, 1251437, 749424, 1245939, 23233 },
			{ 749424, 1245939, 759399, 1251851, 0 },
			{ 759399, 1251851, 755848, 1256721, 23215 } };

	private int[][] road_23215 = { { 762633, 1262729, 755848, 1256721, 0 },
			{ 755848, 1256721, 759399, 1251851, 23224 },
			{ 759399, 1251851, 762346, 1253411, 0 },
			{ 762346, 1253411, 762633, 1262729, 3171 } };

	private int[][] road_23242 = { { 737632, 1248398, 672643, 1244286, 0 },
			{ 672643, 1244286, 673004, 1238297, 5737 },
			{ 673004, 1238297, 737052, 1242349, 0 },
			{ 737052, 1242349, 737632, 1248398, 1916 } };

	private int[][] road_23233 = { { 746933, 1251437, 737632, 1248398, 0 },
			{ 737632, 1248398, 739542, 1242710, 1916 },
			{ 739542, 1242710, 749424, 1245939, 0 },
			{ 749424, 1245939, 746933, 1251437, 23224 } };

	private int[][] road_8734 = { { 762346, 1253411, 764370, 1209068, 0 },
			{ 764370, 1209068, 770394, 1208241, 8228 },
			{ 770394, 1208241, 768990, 1260080, 0 },
			{ 768990, 1260080, 762346, 1253411, 3171 } };

	private int[][] road_29741 = { { 737052, 1242349, 739542, 1242710, 1916 },
			{ 739542, 1242710, 739886, 1232298, 0 },
			{ 739886, 1232298, 737390, 1232119, 29729 },
			{ 737390, 1232119, 737052, 1242349, 0 } };

	private int[] blockade_2144891094 = { 739542, 1242710, 738105, 1246988,
			739115, 1247714, 740246, 1248259, 741477, 1248601, 742786, 1248720,
			743868, 1248639, 744900, 1248404, 746087, 1249451, 747484, 1250219,
			749424, 1245939, 743509, 1246418 };
	private int[] blockade_2144891088 = { 735631, 1242259, 735839, 1243397,
			736219, 1244466, 736756, 1245449, 737433, 1246332, 737081, 1242660,
			737052, 1242349, 736627, 1243830 };
	private int[] blockade_2144891062 = { 749424, 1245939, 747484, 1250219,
			748806, 1250619, 750222, 1250758, 751312, 1250676, 752351, 1250438,
			753326, 1250055, 754226, 1249541, 754205, 1250000, 754195, 1250384,
			754361, 1251928, 754836, 1253356, 755585, 1254634, 756573, 1255726,
			759399, 1251851, 753470, 1250487 };
	private int[] blockade_2144891067 = { 759399, 1251851, 756573, 1255726,
			757602, 1256500, 758763, 1257082, 760033, 1257448, 761387, 1257575,
			762471, 1257494, 762346, 1253411, 759980, 1255046 };
	private int[] blockade_2144890639 = { 762346, 1253411, 762471, 1257494,
			763972, 1257096, 765325, 1256401, 763402, 1255819 };
	private int[] blockade_2144889201 = { 737390, 1232119, 737052, 1242349,
			739542, 1242710, 739886, 1232298, 738471, 1237369 };
	private int[] blockade_2144889722 = { 737052, 1242349, 737433, 1246332,
			738105, 1246988, 739542, 1242710, 738107, 1244231 };
	private int[] blockade_2144888593 = { 770393, 1208241, 769278, 1208394,
			766781, 1242381, 766747, 1243210, 766920, 1245063, 767419, 1246802,
			768212, 1248393, 769268, 1249806, 770394, 1208241, 768758, 1230838 };
	private int[] blockade_2144888592 = { 770393, 1208241, 764370, 1209068,
			763592, 1226093, 765455, 1225775, 767170, 1225104, 768698, 1224118,
			769998, 1222858, 770394, 1208241, 767007, 1216917 };
	private int[] blockade_2144888590 = { 764151, 1213850, 763503, 1228058,
			764818, 1227796, 766036, 1227305, 767131, 1226610, 768080, 1225735,
			768859, 1224704, 769446, 1223539, 769815, 1222265, 769943, 1220906,
			769830, 1219626, 769502, 1218421, 768981, 1217310, 768285, 1216312,
			767435, 1215448, 766449, 1214736, 765348, 1214197, 766381, 1221069 };
	private int[] blockade_2144888591 = { 762828, 1242838, 762346, 1253411,
			765325, 1256401, 766660, 1255273, 767686, 1253855, 768345, 1252205,
			768578, 1250384, 768322, 1248474, 767598, 1246758, 766477, 1245303,
			765024, 1244179, 765204, 1249880 };

	int[] road = { 23242, 1916, 29741, 23233, 23224, 23215, 3171, 8734 };

	int[] blockade = { 2144889722, 2144890639, 2144891062, 2144891067,
			2144891088, 2144891094, 2144888590, 2144888591, 2144888592,
			2144888593, 2144889201 };

	int[] raod_1916_blockade = { 2144889722 };
	int[] raod_3171_blockade = { 2144890639 };
	int[] raod_23224_blockade = { 2144891062 };
	int[] raod_23215_blockade = { 2144891067 };
	int[] raod_23242_blockade = { 2144891088 };
	int[] raod_23233_blockade = { 2144891094 };
	int[] raod_8734_blockade = { 2144888590, 2144888591, 2144888592, 2144888593 };
	int[] raod_29741_blockade = { 2144889201 };

	@Override
	public List<CSURoad> roadList() {
		List<CSURoad> list = new ArrayList<>();

		list.add(createRoadEdge(23242, road_23242));
		list.add(createRoadEdge(1916, road_1916));
		list.add(createRoadEdge(29741, road_29741));
		list.add(createRoadEdge(23233, road_23233));
		list.add(createRoadEdge(23224, road_23224));
		list.add(createRoadEdge(23215, road_23215));
		list.add(createRoadEdge(3171, road_3171));
		list.add(createRoadEdge(8734, road_8734));

		return list;
	}

	@Override
	public Map<Integer, CSUBlockade> blockadeList() {
		Map<Integer, CSUBlockade> list = new TreeMap<>();

		list.put(2144889722, createBlockade(2144889722, blockade_2144889722));
		list.put(2144890639, createBlockade(2144890639, blockade_2144890639));
		list.put(2144891062, createBlockade(2144891062, blockade_2144891062));
		list.put(2144891067, createBlockade(2144891067, blockade_2144891067));
		list.put(2144891088, createBlockade(2144891088, blockade_2144891088));
		list.put(2144891094, createBlockade(2144891094, blockade_2144891094));
		list.put(2144888590, createBlockade(2144888590, blockade_2144888590));
		list.put(2144888591, createBlockade(2144888591, blockade_2144888591));
		list.put(2144888592, createBlockade(2144888592, blockade_2144888592));
		list.put(2144888593, createBlockade(2144888593, blockade_2144888593));
		list.put(2144889201, createBlockade(2144889201, blockade_2144889201));

		return list;
	}

	@Override
	public int[] getBlockadeList(int roadId) {
		switch (roadId) {
		case 1916:
			return raod_1916_blockade;
		case 3171:
			return raod_3171_blockade;
		case 23224:
			return raod_23224_blockade;
		case 23215:
			return raod_23215_blockade;
		case 23242:
			return raod_23242_blockade;
		case 23233:
			return raod_23233_blockade;
		case 8734:
			return raod_8734_blockade;
		case 29741:
			return raod_29741_blockade;
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

		EscapeData_2 es = new EscapeData_2();
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
