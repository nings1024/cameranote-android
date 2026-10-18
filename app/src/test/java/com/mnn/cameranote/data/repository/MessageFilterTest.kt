package com.mnn.cameranote.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessageFilterTest {

    @Test
    fun `空搜索词不过滤`() {
        assertNull(MessageFilter(query = "").escapedQueryOrNull())
        assertNull(MessageFilter(query = "   ").escapedQueryOrNull())
    }

    @Test
    fun `普通关键字原样返回`() {
        assertEquals("看房", MessageFilter(query = " 看房 ").escapedQueryOrNull())
    }

    @Test
    fun `百分号被转义`() {
        assertEquals("""\%""", MessageFilter(query = "%").escapedQueryOrNull())
    }

    @Test
    fun `下划线被转义`() {
        assertEquals("""\_""", MessageFilter(query = "_").escapedQueryOrNull())
    }

    @Test
    fun `反斜杠被双写`() {
        assertEquals("""\\""", MessageFilter(query = """\""").escapedQueryOrNull())
    }

    @Test
    fun `混合输入按序转义`() {
        assertEquals(
            """a\%b\_c\\d""",
            MessageFilter(query = """a%b_c\d""").escapedQueryOrNull(),
        )
    }
}
