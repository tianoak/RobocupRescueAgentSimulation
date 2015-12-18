package csu.common.test.escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rescuecore2.worldmodel.EntityID;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;

public class EscapeData_6 implements EscapeData{

	private int[][] road_17635 = {{531851, 228942, 544597, 232164, 0}, {544597, 232164, 542985, 236680, 3279}, {542985, 236680, 530208, 233684, 0}, {530208, 233684, 531851, 228942, 17662}};
	private int[][] road_12316 = {{684443, 693124, 677618, 765952, 0}, {677618, 765952, 671644, 765395, 6743}, {671644, 765395, 678184, 695598, 0}, {678184, 695598, 684443, 693124, 6602}};
	private int[][] road_21433 = {{784944, 251094, 747988, 248343, 0}, {747988, 248343, 746965, 239241, 7527}, {746965, 239241, 785038, 242077, 0}, {785038, 242077, 784944, 251094, 8029}};
	private int[][] road_14602 = {{853654, 255841, 851215, 299289, 0}, {851215, 299289, 840879, 302248, 2587}, {840879, 302248, 847668, 255430, 0}, {847668, 255430, 853654, 255841, 6654}};


	private int[] blockade_2138272192 = {784998, 245875, 783224, 246018, 782342, 246179, 781951, 247965, 772495, 245890, 772294, 245950, 771058, 246471, 768819, 247891, 766949, 249754, 784944, 251094, 784998, 245875, 777053, 248598};
	private int[] blockade_2138271891 = {532655, 231361, 532508, 231532, 531904, 232505, 531459, 233574, 542103, 236071, 543451, 232295, 542638, 232089, 541091, 235356, 532782, 231421, 532655, 231361, 537006, 233773};
	private int[] blockade_2138272476 = {676674, 711703, 675840, 720613, 676504, 719433, 676997, 718157, 677304, 716800, 677410, 715377, 677220, 713475, 676693, 716222};
	private int[] blockade_2138272475 = {678092, 696569, 677360, 704387, 677891, 703305, 678283, 702151, 678526, 700936, 678610, 699671, 678477, 698077, 678056, 700526};
	private int[] blockade_2138272474 = {676663, 733758, 676621, 733827, 676160, 734924, 675871, 736101, 675772, 737341, 675853, 738458, 676087, 739525, 676464, 740531, 676972, 741464, 676074, 742588, 675398, 743870, 674971, 745281, 674822, 746793, 674905, 747923, 675145, 749002, 675530, 750018, 676050, 750959, 675166, 752078, 674500, 753352, 674080, 754753, 673934, 756252, 674221, 758338, 675029, 760206, 676281, 761777, 677897, 762972, 680100, 739404, 676946, 738892, 677301, 736699, 677769, 733814, 676663, 733758, 677043, 749375};
	private int[] blockade_2138272325 = {853179, 264289, 851145, 266060, 849526, 268222, 848392, 270707, 848029, 272048, 847813, 273444, 847313, 277920, 847628, 277915, 847819, 288373, 844752, 288428, 844752, 288428, 843951, 290008, 843233, 292107, 842890, 294353, 842586, 298622, 842569, 298857, 842358, 301824, 851214, 299289, 851215, 299289, 853179, 264289, 848667, 286145};


	int[] road = { 17635, 21433, 14602, 12316 };
	int[] blockade = {2138271891, 2138272192, 2138272325, 2138272474, 2138272475, 2138272476};
	
	int[] raod_17635_blockade = { 2138271891 };
	int[] raod_21433_blockade = { 2138272192 };
	int[] raod_14602_blockade = { 2138272325 };
	int[] raod_12316_blockade = { 2138272474, 2138272475, 2138272476 };
	
	@Override
	public List<CSURoad> roadList() {
		List<CSURoad> list = new ArrayList<>();

		list.add(createRoadEdge(17635, road_17635));
		list.add(createRoadEdge(21433, road_21433));
		list.add(createRoadEdge(14602, road_14602));
		list.add(createRoadEdge(12316, road_12316));
		
		return list;
	}

	@Override
	public Map<Integer, CSUBlockade> blockadeList() {
		Map<Integer, CSUBlockade> list = new TreeMap<>();

		list.put(2138271891, createBlockade(2138271891, blockade_2138271891));
		list.put(2138272192, createBlockade(2138272192, blockade_2138272192));
		list.put(2138272325, createBlockade(2138272325, blockade_2138272325));
		list.put(2138272474, createBlockade(2138272474, blockade_2138272474));
		list.put(2138272475, createBlockade(2138272475, blockade_2138272475));
		list.put(2138272476, createBlockade(2138272476, blockade_2138272476));
		
		return list;
	}

	@Override
	public int[] getBlockadeList(int roadId) {
		switch (roadId) {
		case 17635:
			return raod_17635_blockade;
		case 21433:
			return raod_21433_blockade;
		case 14602:
			return raod_14602_blockade;
		case 12316:
			return raod_12316_blockade;
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

		EscapeData_6 es = new EscapeData_6();
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
