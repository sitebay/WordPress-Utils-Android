package org.sitebay.android.ui.sitecreation.domains

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.junit.Test

class SiteCreationDomainSanitizerTest {
    private val domainSanitizer = SiteCreationDomainSanitizer()

    @Test
    fun `Verify everything after the first period is removed`() {
        val result = domainSanitizer.sanitizeDomainQuery("test.sitebay.com")
        assertFalse(result.contains("sitebay.com"))
    }

    @Test
    fun `Verify that a word doesn't break the sanitizer and its value is returned`() {
        val result = domainSanitizer.sanitizeDomainQuery("test")
        assertEquals(result, "test")
    }

    @Test
    fun `Remove https if its present`() {
        val result = domainSanitizer.sanitizeDomainQuery("https://test.sitebay.com")
        assertFalse(result.contains("https://"))
    }

    @Test
    fun `Remove http if its present`() {
        val result = domainSanitizer.sanitizeDomainQuery("http://test.sitebay.com")
        assertFalse(result.contains("http://"))
    }

    @Test
    fun `Remove all characters that are not alphanumeric`() {
        val result = domainSanitizer.sanitizeDomainQuery("test_this-site.sitebay.com")
        assertEquals(result, "testthissite")
    }

    @Test
    fun `Get first domain part`() {
        val result = domainSanitizer.getName("https://test_this-site.sitebay.com")
        assertEquals(result, "test_this-site")
    }

    @Test
    fun `Get second domain part`() {
        val result = domainSanitizer.getDomain("https://test_this-site.sitebay.com")
        assertEquals(result, ".sitebay.com")
    }
}
