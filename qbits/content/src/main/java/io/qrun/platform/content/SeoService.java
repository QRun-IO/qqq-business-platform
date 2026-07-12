package io.qrun.platform.content;

import java.time.Instant;
import java.util.List;

public final class SeoService {
   public record SitemapEntry(String path, PublicationState state, Instant modifiedAt) {}
   public String metaTags(String title, String description, String canonicalUrl) {
      return "<title>" + escape(title) + "</title><meta name=\"description\" content=\"" + escape(description) + "\"><link rel=\"canonical\" href=\"" + escape(canonicalUrl) + "\">";
   }
   public String articleStructuredData(String title, String url, Instant publishedAt, String author) {
      return "{\"@context\":\"https://schema.org\",\"@type\":\"Article\",\"headline\":\"" + json(title) + "\",\"url\":\"" + json(url) + "\",\"datePublished\":\"" + publishedAt + "\",\"author\":{\"@type\":\"Person\",\"name\":\"" + json(author) + "\"}}";
   }
   public String sitemap(String baseUrl, List<SitemapEntry> entries) {
      StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
      entries.stream().filter(e -> e.state() == PublicationState.PUBLISHED).forEach(e -> xml.append("<url><loc>").append(escape(baseUrl.replaceAll("/$", "") + "/" + e.path().replaceAll("^/", ""))).append("</loc><lastmod>").append(e.modifiedAt()).append("</lastmod></url>"));
      return xml.append("</urlset>").toString();
   }
   private String escape(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
   private String json(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
