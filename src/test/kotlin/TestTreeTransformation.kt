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
}