package io.qrun.platform.content.model;
import java.util.List;
public final class ContentTables {
   public static final String SITE="cmsSite", PAGE="cmsPage", BLOG_POST="cmsBlogPost", AUTHOR="cmsAuthor", TAG="cmsTag", CATEGORY="cmsCategory", ASSET="cmsAsset", NAVIGATION="cmsNavigation", REDIRECT="cmsRedirect", CONTENT_REVISION="cmsContentRevision";
   public static final List<String> ALL=List.of(SITE,PAGE,BLOG_POST,AUTHOR,TAG,CATEGORY,ASSET,NAVIGATION,REDIRECT,CONTENT_REVISION);
   private ContentTables() {}
}
