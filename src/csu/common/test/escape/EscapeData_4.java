package csu.common.test.escape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rescuecore2.worldmodel.EntityID;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;

public class EscapeData_4 implements EscapeData {
	private int[][] road_53009 = { { 626987, 589420, 624891, 599284, 15655 },
			{ 624891, 599284, 624994, 589309, 15646 },
			{ 624994, 589309, 626987, 589420, 40143 } };

	private int[][] road_815 = { { 566462, 594634, 568291, 584748, 15664 },
			{ 568291, 584748, 568956, 594832, 15673 },
			{ 568956, 594832, 566462, 594634, 37979 } };

	private int[][] road_868 = { { 638299, 600352, 639072, 590382, 15655 },
			{ 639072, 590382, 641568, 590581, 48051 },
			{ 641568, 590581, 640796, 600551, 15700 },
			{ 640796, 600551, 638299, 600352, 34242 } };

	private int[][] road_1396 = { { 551703, 583427, 553475, 583591, 0 },
			{ 553475, 583591, 555214, 583707, 25409 },
			{ 555214, 583707, 553150, 593574, 15664 },
			{ 553150, 593574, 551703, 583427, 15619 } };

	private int[][] road_3347 = { { 478311, 587569, 479176, 577606, 15583 },
			{ 479176, 577606, 485153, 578131, 12028 },
			{ 485153, 578131, 484289, 588094, 15574 },
			{ 484289, 588094, 478311, 587569, 12019 } };

	private int[][] road_3686 = { { 537300, 592313, 539594, 582464, 15610 },
			{ 539594, 582464, 539795, 592511, 15619 },
			{ 539795, 592511, 537300, 592313, 31935 } };

	private int[][] road_3902 = { { 523498, 581183, 526002, 581382, 36534 },
			{ 526002, 581382, 524453, 591290, 15610 },
			{ 524453, 591290, 523498, 581183, 15637 } };

	private int[][] road_6014 = { { 595820, 586939, 598312, 587137, 44461 },
			{ 598312, 587137, 596195, 597000, 15646 },
			{ 596195, 597000, 595820, 586939, 15673 } };

	private int[][] road_6636 = { { 508877, 590051, 510897, 580180, 15628 },
			{ 510897, 580180, 511370, 590249, 15637 },
			{ 511370, 590249, 508877, 590051, 35273 } };

	private int[][] road_12028 = { { 491401, 491390, 485153, 578131, 0 },
			{ 485153, 578131, 479176, 577606, 3347 },
			{ 479176, 577606, 485438, 490658, 0 },
			{ 485438, 490658, 491401, 491390, 3441 } };

	private int[][] road_15655 = { { 638299, 600352, 624891, 599284, 0 },
			{ 624891, 599284, 626987, 589420, 53009 },
			{ 626987, 589420, 639072, 590382, 0 },
			{ 639072, 590382, 638299, 600352, 868 } };

	private int[][] road_15673 = { { 596195, 597000, 568956, 594832, 0 },
			{ 568956, 594832, 568291, 584748, 815 },
			{ 568291, 584748, 595820, 586939, 0 },
			{ 595820, 586939, 596195, 597000, 6014 } };

	private int[][] road_15664 = { { 566462, 594634, 553150, 593574, 0 },
			{ 553150, 593574, 555214, 583707, 1396 },
			{ 555214, 583707, 568291, 584748, 0 },
			{ 568291, 584748, 566462, 594634, 815 } };

	private int[][] road_15628 = { { 508877, 590051, 495756, 589006, 0 },
			{ 495756, 589006, 498288, 579176, 7220 },
			{ 498288, 579176, 510897, 580180, 0 },
			{ 510897, 580180, 508877, 590051, 6636 } };

	private int[][] road_15619 = { { 553150, 593574, 539795, 592511, 0 },
			{ 539795, 592511, 539594, 582464, 3686 },
			{ 539594, 582464, 551703, 583427, 0 },
			{ 551703, 583427, 553150, 593574, 1396 } };

	private int[][] road_15646 = { { 624891, 599284, 596195, 597000, 0 },
			{ 596195, 597000, 598312, 587137, 6014 },
			{ 598312, 587137, 624994, 589309, 0 },
			{ 624994, 589309, 624891, 599284, 53009 } };

	private int[][] road_15637 = { { 524453, 591290, 511370, 590249, 0 },
			{ 511370, 590249, 510897, 580180, 6636 },
			{ 510897, 580180, 523498, 581183, 0 },
			{ 523498, 581183, 524453, 591290, 3902 } };

	private int[][] road_15700 = { { 677324, 603458, 640796, 600551, 0 },
			{ 640796, 600551, 641568, 590581, 868 },
			{ 641568, 590581, 678105, 593489, 0 },
			{ 678105, 593489, 677324, 603458, 5167 } };

	private int[][] road_15610 = { { 537300, 592313, 524453, 591290, 0 },
			{ 524453, 591290, 526002, 581382, 3902 },
			{ 526002, 581382, 539594, 582464, 0 },
			{ 539594, 582464, 537300, 592313, 3686 } };

	private int[][] road_15574 = { { 495756, 589006, 484289, 588094, 0 },
			{ 484289, 588094, 485153, 578131, 3347 },
			{ 485153, 578131, 495784, 578977, 0 },
			{ 495784, 578977, 495756, 589006, 7220 } };

	private int[] blockade_2144891027 = { 536732, 587859, 534548, 588133,
			532565, 588907, 530851, 590115, 529472, 591689, 537300, 592313,
			538304, 588000, 534572, 590215 };
	private int[] blockade_2144891028 = { 526002, 581382, 525922, 581888,
			526780, 582696, 527793, 583309, 528932, 583699, 530167, 583836,
			531350, 583711, 532446, 583352, 533430, 582787, 534275, 582040,
			529861, 582558 };
	private int[] blockade_2144891034 = { 485153, 578131, 484800, 582198,
			485954, 582992, 487238, 583587, 488627, 583960, 490102, 584090,
			491438, 583984, 492707, 583676, 494180, 584213, 495768, 584466,
			495784, 578977, 490454, 581194 };
	private int[] blockade_2144890968 = { 641568, 590581, 641293, 594131,
			641654, 594154, 641802, 594163, 657596, 595160, 658238, 595182,
			660084, 594990, 661799, 594439, 663342, 593569, 664673, 592420,
			651821, 593032 };
	private int[] blockade_2144890891 = { 626987, 589420, 626883, 589906,
			626340, 592461, 628032, 593114, 629870, 593410, 638793, 593973,
			639072, 590382, 632821, 591718 };
	private int[] blockade_2144890892 = { 626987, 589420, 626831, 590150,
			627109, 590164, 627188, 590168, 630029, 590311, 630364, 590320,
			631526, 590203, 632609, 589867, 629266, 589919 };
	private int[] blockade_2144890904 = { 539724, 588997, 538498, 590192,
			537473, 591568, 537300, 592313, 539795, 592511, 538831, 591191 };
	private int[] blockade_2144890905 = { 538304, 588000, 537300, 592313,
			539795, 592511, 539705, 588038, 539232, 588025, 538563, 588050,
			538758, 590428 };
	private int[] blockade_2144890911 = { 498288, 579176, 496926, 584459,
			497025, 584452, 498808, 584642, 499936, 584566, 501020, 584346,
			502572, 584821, 504237, 584987, 506045, 584791, 507718, 584231,
			509212, 583349, 510486, 582186, 510897, 580180, 503550, 582084 };
	private int[] blockade_2144890897 = { 568810, 587974, 568504, 587978,
			568956, 594832, 596195, 597000, 595924, 589750, 595581, 589745,
			594130, 589841, 592738, 590122, 591417, 590576, 590180, 591189,
			588868, 590476, 587454, 589947, 585954, 589618, 584385, 589505,
			582409, 589685, 580553, 590204, 578848, 591029, 577326, 592130,
			575648, 590413, 573624, 589103, 571322, 588267, 570088, 588048,
			582708, 592861 };
	private int[] blockade_2144890898 = { 568291, 584748, 568379, 586085,
			569702, 585667, 570861, 584952, 569232, 585298 };
	private int[] blockade_2144890899 = { 566315, 587809, 564789, 587915,
			563329, 588226, 561949, 588727, 560664, 589403, 559488, 590239,
			558436, 591221, 557522, 592334, 556761, 593564, 555623, 591328,
			554016, 589430, 553150, 593573, 553150, 593574, 566462, 594634,
			567708, 587898, 561778, 591574 };
	private int[] blockade_2144890900 = { 553512, 591840, 553150, 593574,
			554644, 593692, 553768, 593035 };
	private int[] blockade_2144890901 = { 555214, 583707, 554861, 585391,
			555541, 585432, 556581, 585335, 557558, 585058, 558453, 584616,
			559249, 584028, 556639, 584529 };
	private int[] blockade_2144890902 = { 563226, 584344, 564074, 585105,
			565065, 585682, 566172, 586048, 567368, 586176, 568033, 586137,
			568291, 584748, 566163, 585220 };
	private int[] blockade_2144890914 = { 548683, 583186, 549350, 583883,
			550128, 584456, 551000, 584890, 551951, 585168, 551703, 583427,
			550653, 584012 };
	private int[] blockade_2144890913 = { 539705, 588038, 539795, 592511,
			553150, 593574, 552787, 591035, 551493, 589983, 550011, 589190,
			548378, 588689, 546626, 588514, 544989, 588666, 543454, 589104,
			541666, 588367, 546119, 590951 };
	private int[] blockade_2144890912 = { 546626, 586506, 544703, 586676,
			542893, 587168, 541223, 587951, 539724, 588997, 539795, 592511,
			553149, 593573, 553150, 593573, 552378, 588163, 551074, 587461,
			549670, 586941, 548182, 586617, 546557, 590120 };
	private int[] blockade_2144890919 = { 607051, 587848, 607828, 588441,
			608701, 588897, 609653, 589201, 610670, 589336, 624610, 590038,
			624688, 590042, 624986, 590057, 624994, 589309, 615862, 589058 };
	private int[] blockade_2144890918 = { 622758, 589127, 623749, 590467,
			624970, 591598, 624994, 589309, 624202, 590046 };
	private int[] blockade_2144890917 = { 597707, 589954, 596195, 597000,
			614099, 598425, 613517, 596730, 612674, 595176, 611597, 593789,
			610312, 592595, 608847, 591620, 607226, 590891, 605477, 590435,
			603626, 590277, 601901, 590413, 600264, 590810, 599021, 590304,
			604536, 594441 };
	private int[] blockade_2144890573 = { 485846, 568506, 484163, 569816,
			482863, 571507, 482026, 573500, 481805, 574586, 481729, 575718,
			482004, 577854, 485153, 578131, 483895, 574026 };
	private int[] blockade_2144890572 = { 486809, 555135, 485313, 556448,
			484165, 558079, 483429, 559966, 483169, 562043, 483358, 563817,
			483896, 565460, 484746, 566934, 485868, 568199, 485070, 561586 };
	private int[] blockade_2144890783 = { 482004, 577854, 482444, 579108,
			483069, 580262, 483860, 581298, 484800, 582198, 485153, 578131,
			483864, 579516 };
	private int[] blockade_2144889019 = { 624994, 589309, 624970, 591598,
			626340, 592461, 626987, 589420, 625878, 590643 };
	private int[] blockade_2144889020 = { 624994, 589309, 624986, 590057,
			626831, 590150, 626987, 589420, 625947, 589729 };
	private int[] blockade_2144889179 = { 639072, 590382, 638793, 593973,
			639152, 593996, 639300, 594005, 641293, 594131, 641568, 590581,
			640179, 592266 };
	private int[] blockade_2144889136 = { 567708, 587898, 566462, 594634,
			568956, 594832, 568504, 587978, 568277, 587986, 567872, 591923 };
	private int[] blockade_2144889436 = { 552378, 588163, 553150, 593574,
			554016, 589430, 553181, 590389 };
	private int[] blockade_2144889438 = { 551703, 583427, 551951, 585168,
			553045, 585275, 553949, 585202, 554861, 585391, 555214, 583707,
			553475, 583591, 553418, 584388 };
	private int[] blockade_2144889583 = { 595924, 589750, 596195, 597000,
			597707, 589954, 596608, 592234 };

	int[] raod_53009_blockade = { 2144889019, 2144889020 };
	int[] raod_815_blockade = { 2144889136 };
	int[] raod_868_blockade = { 2144889179 };
	int[] raod_1396_blockade = { 2144889436, 2144889438 };
	int[] raod_3347_blockade = { 2144890783 };
	int[] raod_3686_blockade = { 2144890904, 2144890905 };
	int[] raod_6014_blockade = { 2144889583 };
	int[] raod_12028_blockade = { 2144890572, 2144890573 };
	int[] raod_15655_blockade = { 2144890891, 2144890892 };
	int[] raod_15673_blockade = { 2144890897, 2144890898 };
	int[] raod_15664_blockade = { 2144890899, 2144890900, 2144890901, 2144890902 };
	int[] raod_15628_blockade = { 2144890911 };
	int[] raod_15619_blockade = { 2144890912, 2144890913, 2144890914 };
	int[] raod_15646_blockade = { 2144890917, 2144890918, 2144890919 };
	int[] raod_15700_blockade = { 2144890968 };
	int[] raod_15610_blockade = { 2144891027, 2144891028 };
	int[] road_15574_blockade = { 2144891034 };

	int[] road = { 12028, 3347, 15574, 15628, 6636, 15637, 3902, 15610, 3686,
			15619, 1396, 15664, 815, 15673, 6014, 15646, 53009, 15655, 868,
			15700 };

	int[] blockade = { 2144889019, 2144889020, 2144889136, 2144889179,
			2144889436, 2144889438, 2144890783, 2144890904, 2144890905,
			2144889583, 2144890572, 2144890573, 2144890891, 2144890892,
			2144890897, 2144890898, 2144890899, 2144890900, 2144890901,
			2144890902, 2144890911, 2144890912, 2144890913, 2144890914,
			2144890917, 2144890918, 2144890919, 2144890968, 2144891027,
			2144891028, 2144891034 };

	@Override
	public List<CSURoad> roadList() {
		List<CSURoad> list = new ArrayList<>();

		list.add(createRoadEdge(12028, road_12028));
		list.add(createRoadEdge(3347, road_3347));
		list.add(createRoadEdge(15574, road_15574));
		list.add(createRoadEdge(15628, road_15628));
		list.add(createRoadEdge(6636, road_6636));
		list.add(createRoadEdge(15637, road_15637));
		list.add(createRoadEdge(3902, road_3902));
		list.add(createRoadEdge(15610, road_15610));
		list.add(createRoadEdge(3686, road_3686));
		list.add(createRoadEdge(15619, road_15619));
		list.add(createRoadEdge(1396, road_1396));
		list.add(createRoadEdge(15664, road_15664));
		list.add(createRoadEdge(815, road_815));
		list.add(createRoadEdge(15673, road_15673));
		list.add(createRoadEdge(6014, road_6014));
		list.add(createRoadEdge(15646, road_15646));
		list.add(createRoadEdge(53009, road_53009));
		list.add(createRoadEdge(15655, road_15655));
		list.add(createRoadEdge(868, road_868));
		list.add(createRoadEdge(15700, road_15700));

		return list;
	}

	@Override
	public Map<Integer, CSUBlockade> blockadeList() {
		Map<Integer, CSUBlockade> list = new TreeMap<>();

		list.put(2144889019, createBlockade(2144889019, blockade_2144889019));
		list.put(2144889020, createBlockade(2144889020, blockade_2144889020));
		list.put(2144889136, createBlockade(2144889136, blockade_2144889136));
		list.put(2144889179, createBlockade(2144889179, blockade_2144889179));
		list.put(2144889436, createBlockade(2144889436, blockade_2144889436));
		list.put(2144889438, createBlockade(2144889438, blockade_2144889438));
		list.put(2144890783, createBlockade(2144890783, blockade_2144890783));
		list.put(2144890904, createBlockade(2144890904, blockade_2144890904));
		list.put(2144890905, createBlockade(2144890905, blockade_2144890905));
		list.put(2144889583, createBlockade(2144889583, blockade_2144889583));
		list.put(2144890572, createBlockade(2144890572, blockade_2144890572));
		list.put(2144890573, createBlockade(2144890573, blockade_2144890573));
		list.put(2144890891, createBlockade(2144890891, blockade_2144890891));
		list.put(2144890892, createBlockade(2144890892, blockade_2144890892));
		list.put(2144890897, createBlockade(2144890897, blockade_2144890897));
		list.put(2144890898, createBlockade(2144890898, blockade_2144890898));
		list.put(2144890899, createBlockade(2144890899, blockade_2144890899));
		list.put(2144890900, createBlockade(2144890900, blockade_2144890900));
		list.put(2144890901, createBlockade(2144890901, blockade_2144890901));
		list.put(2144890902, createBlockade(2144890902, blockade_2144890902));
		list.put(2144890911, createBlockade(2144890911, blockade_2144890911));
		list.put(2144890912, createBlockade(2144890912, blockade_2144890912));
		list.put(2144890913, createBlockade(2144890913, blockade_2144890913));
		list.put(2144890914, createBlockade(2144890914, blockade_2144890914));
		list.put(2144890917, createBlockade(2144890917, blockade_2144890917));
		list.put(2144890918, createBlockade(2144890918, blockade_2144890918));
		list.put(2144890919, createBlockade(2144890919, blockade_2144890919));
		list.put(2144890968, createBlockade(2144890968, blockade_2144890968));
		list.put(2144891027, createBlockade(2144891027, blockade_2144891027));
		list.put(2144891028, createBlockade(2144891028, blockade_2144891028));
		list.put(2144891034, createBlockade(2144891034, blockade_2144891034));

		return list;
	}

	@Override
	public int[] getBlockadeList(int roadId) {
		switch (roadId) {
		case 12028:
			return raod_12028_blockade;
		case 3347:
			return raod_3347_blockade;
		case 15574:
			int[] a = {};
			return a;
		case 15628:
			return raod_15628_blockade;
		case 6636:
			int[] b = {};
			return b;
		case 15637:
			int[] c = {};
			return c;
		case 3902:
			int[] d = {};
			return d;
		case 15610:
			return raod_15610_blockade;
		case 3686:
			return raod_3686_blockade;
		case 15619:
			return raod_15619_blockade;
		case 1396:
			return raod_1396_blockade;
		case 15664:
			return raod_15664_blockade;
		case 815:
			return raod_815_blockade;
		case 15673:
			return raod_15673_blockade;
		case 6014:
			return raod_6014_blockade;
		case 15646:
			return raod_15646_blockade;
		case 53009:
			return raod_53009_blockade;
		case 15655:
			return raod_15655_blockade;
		case 868:
			return raod_868_blockade;
		case 15700:
			return raod_15700_blockade;
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

		EscapeData_4 es = new EscapeData_4();
		// es.print_road();
		System.out.println();
		es.print_blockade();
		System.out.println();
		// es.printAssignBlockade();
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
