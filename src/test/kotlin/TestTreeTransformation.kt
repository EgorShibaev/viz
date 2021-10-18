import kotlin.random.Random
import kotlin.test.*

class TestTreeTransformation {

	@Test
	fun testEmptyList() {
		assertEquals(emptyList(), transformTreeCells(emptyList()))
	}

	@Test
	fun testOneNode() {
		val node = TreeCell(emptyList(), "1")
		assertEquals(listOf(node), transformTreeCells(listOf(node)))
	}

	@Test
	fun testTwoNodes() {
		val root = TreeCell(listOf(TreeCell(emptyList(), "2")), "1")
		val child = TreeCell(emptyList(), "2")
		assertEquals(TreeCell(listOf(child), "1"), transformTreeCells(listOf(child, root))!![0])
	}

	@Test
	fun testTwoSeparatedNodes() {
		val node1 = TreeCell(emptyList(), "1")
		val node2 = TreeCell(emptyList(), "2")
		assertEquals(listOf(node1, node2), transformTreeCells(listOf(node1, node2)))
	}

	@Test
	fun testDuplicateNames() {
		val node1 = TreeCell(listOf(TreeCell(emptyList(), "1", "")), "1")
		val node2 = TreeCell(emptyList(), "1")
		assertEquals(null, transformTreeCells(listOf(node1, node2)))
	}

	@Test
	fun testCircle() {
		val node1 = TreeCell(listOf(TreeCell(emptyList(), "2")), "1")
		val node2 = TreeCell(listOf(TreeCell(emptyList(), "3")), "2")
		val node3 = TreeCell(listOf(TreeCell(emptyList(), "1")), "3")
		assertEquals(null, transformTreeCells(listOf(node1, node2, node3)))
	}

	@Test
	fun testPath() {
		val node1 = TreeCell(listOf(TreeCell(emptyList(), "2")), "1")
		val node2 = TreeCell(listOf(TreeCell(emptyList(), "3")), "2")
		val node3 = TreeCell(listOf(TreeCell(emptyList(), "4")), "3")
		val node4 = TreeCell(emptyList(), "4")
		val path34 = TreeCell(listOf(node4), "3")
		val path12 = TreeCell(listOf(TreeCell(listOf(path34), "2")), "1")
		assertEquals(path12, transformTreeCells(listOf(node1, node2, node3, node4))!![0])
	}

	@Test
	fun testUnknownChild() {
		val node1 = TreeCell(listOf(TreeCell(emptyList(), "unknown name")), "1")
		val node2 = TreeCell(emptyList(), "2")
		assertEquals(null, transformTreeCells(listOf(node1, node2)))
	}

	@Test
	fun testTwoPath() {
		val node3 = TreeCell(emptyList(), "3")
		val node2 = TreeCell(listOf(node3), "2")
		val node1 = TreeCell(listOf(node2, node3), "1")
		assertEquals(null, transformTreeCells(listOf(node1, node2, node3)))
	}

	@Test
	fun testTwoParent() {
		val node3 = TreeCell(emptyList(), "3")
		val node2 = TreeCell(listOf(node3), "2")
		val node1 = TreeCell(listOf(node3), "1")
		assertEquals(null, transformTreeCells(listOf(node1, node2, node3)))
	}

	private fun generateRandomTree(depth: Int): TreeCell {
		val name = "name${Random.nextInt()}"
		if (depth == 1)
			return TreeCell(emptyList(), name)
		val childrenCount = Random.nextInt(1, 4)
		val children = (1..childrenCount).map { generateRandomTree(depth - 1) }
		return TreeCell(children, name)
	}

	private fun getListOfNodes(root: TreeCell): List<TreeCell> {
		val rootNode = TreeCell(root.children.map { TreeCell(emptyList(), it.name) }, root.name)
		return listOf(rootNode) + root.children.map { getListOfNodes(it) }.flatten()
	}

	@Test
	fun testRandomTree() {
		val testCount = 10
		repeat(testCount) {
			val randomTree = generateRandomTree(3)
			val listOfNodes = getListOfNodes(randomTree)
			assertEquals(randomTree, transformTreeCells(listOfNodes)!![0])
		}
	}
}