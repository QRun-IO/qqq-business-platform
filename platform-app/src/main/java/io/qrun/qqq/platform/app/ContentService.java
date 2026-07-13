package io.qrun.qqq.platform.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qrun.platform.content.SlugService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ContentService {
  private static final UUID DEFAULT_SITE=UUID.nameUUIDFromBytes("qqq-default-site".getBytes());
  private final JdbcTemplate db; private final ObjectMapper json; private final SlugService slugs=new SlugService();
  ContentService(JdbcTemplate db,ObjectMapper json){this.db=db;this.json=json;}
  Page<PostView> publicPosts(int page,int size){return list("where p.publication_state='published' and p.published_at <= current_timestamp",page,size);}
  Page<PostView> allPosts(int page,int size){return list("",page,size);}
  PostView publicPost(UUID id){return one(id,"and p.publication_state='published' and p.published_at <= current_timestamp");}
  PostView publicPost(String slug){List<PostView> rows=db.query(select()+" where p.slug=? and p.publication_state='published' and p.published_at <= current_timestamp",(rs,n)->map(rs),slug);if(rows.isEmpty())throw ApiException.notFound("post");return rows.getFirst();}
  PostView allPost(UUID id){return one(id,"");}

  @Transactional PostView create(long userId,ApiModels.PostRequest request){requireArray(request.content());ensureSite();UUID authorId=ensureAuthor(userId),id=UUID.randomUUID();Instant now=Instant.now();String slug=uniqueSlug(request.slug()==null||request.slug().isBlank()?request.title():request.slug(),null);db.update("insert into cms_blog_post(id,site_id,author_id,title,slug,excerpt,content,featured_image_asset_id,publication_state,created_at,updated_at) values(?,?,?,?,?,?,?::jsonb,?,'draft',?,?)",id,DEFAULT_SITE,authorId,request.title(),slug,request.excerpt(),encode(request.content()),request.featuredImageAssetId(),Timestamp.from(now),Timestamp.from(now));replaceTaxonomy(id,request.categoryIds(),request.tagIds());return one(id,"");}
  @Transactional PostView updateOwn(UUID id,long userId,ApiModels.PostRequest request){requireArray(request.content());UUID authorId=authorFor(userId);String slug=uniqueSlug(request.slug()==null||request.slug().isBlank()?request.title():request.slug(),id);int changed=db.update("update cms_blog_post set title=?,slug=?,excerpt=?,content=?::jsonb,featured_image_asset_id=?,updated_at=current_timestamp where id=? and author_id=?",request.title(),slug,request.excerpt(),encode(request.content()),request.featuredImageAssetId(),id,authorId);if(changed==0)throw ApiException.notFound("post");replaceTaxonomy(id,request.categoryIds(),request.tagIds());return one(id,"");}
  @Transactional void deleteOwn(UUID id,long userId){UUID authorId=authorFor(userId);if(db.update("delete from cms_blog_post where id=? and author_id=?",id,authorId)==0)throw ApiException.notFound("post");}
  @Transactional PostView publish(UUID id){if(db.update("update cms_blog_post set publication_state='published',published_at=current_timestamp,updated_at=current_timestamp where id=?",id)==0)throw ApiException.notFound("post");return one(id,"");}
  @Transactional PostView unpublish(UUID id){if(db.update("update cms_blog_post set publication_state='draft',published_at=null,updated_at=current_timestamp where id=?",id)==0)throw ApiException.notFound("post");return one(id,"");}

  List<TaxonomyView> categories(){return taxonomy("cms_category");} List<TaxonomyView> tags(){return taxonomy("cms_tag");}
  @Transactional TaxonomyView createTaxonomy(String table,ApiModels.TaxonomyRequest request){ensureSite();UUID id=UUID.randomUUID();String slug=slugs.slugify(request.slug()==null||request.slug().isBlank()?request.name():request.slug());db.update("insert into "+allowedTaxonomy(table)+"(id,site_id,name,slug,description,created_at,updated_at) values(?,?,?,?,?,current_timestamp,current_timestamp)",id,DEFAULT_SITE,request.name(),slug,request.description());return new TaxonomyView(id,request.name(),slug,request.description());}
  @Transactional void deleteTaxonomy(String table,UUID id){if(db.update("delete from "+allowedTaxonomy(table)+" where id=?",id)==0)throw ApiException.notFound("taxonomy");}

  private Page<PostView> list(String where,int page,int size){int p=Math.max(0,page),s=Math.min(100,Math.max(1,size));long total=db.queryForObject("select count(*) from cms_blog_post p "+where,Long.class);List<PostView> items=db.query(select()+" "+where+" order by coalesce(p.published_at,p.created_at) desc limit ? offset ?",(rs,n)->map(rs),s,p*s);return new Page<>(items,p,s,total,(total+s-1)/s);}
  private PostView one(UUID id,String extra){List<PostView> rows=db.query(select()+" where p.id=? "+extra,(rs,n)->map(rs),id);if(rows.isEmpty())throw ApiException.notFound("post");return rows.getFirst();}
  private String select(){return "select p.id,p.title,p.slug,p.excerpt,p.content::text,p.featured_image_asset_id,p.publication_state,p.published_at,p.created_at,p.updated_at,a.display_name author_name from cms_blog_post p join cms_author a on a.id=p.author_id";}
  private PostView map(ResultSet rs)throws SQLException{try{UUID id=rs.getObject("id",UUID.class);return new PostView(id,rs.getString("title"),rs.getString("slug"),rs.getString("excerpt"),json.readTree(rs.getString("content")),rs.getString("publication_state"),instant(rs,"published_at"),instant(rs,"created_at"),instant(rs,"updated_at"),rs.getString("author_name"),ids("cms_blog_post_category","category_id",id),ids("cms_blog_post_tag","tag_id",id),rs.getObject("featured_image_asset_id",UUID.class));}catch(JsonProcessingException e){throw new SQLException("Invalid content JSON",e);}}
  private Instant instant(ResultSet rs,String name)throws SQLException{Timestamp value=rs.getTimestamp(name);return value==null?null:value.toInstant();}
  private void requireArray(JsonNode content){if(content==null||!content.isArray())throw new ApiException(HttpStatus.BAD_REQUEST,"invalid_content","content must be a JSON array");}
  private String encode(JsonNode node){try{return json.writeValueAsString(node);}catch(JsonProcessingException e){throw new IllegalArgumentException("Invalid content",e);}}
  private void ensureSite(){db.update("insert into cms_site(id,name,default_locale,created_at,updated_at) values(?,?,'en',current_timestamp,current_timestamp) on conflict(id) do nothing",DEFAULT_SITE,"Default Site");}
  private UUID ensureAuthor(long userId){List<UUID> ids=db.query("select id from cms_author where user_profile_id=?",(rs,n)->rs.getObject(1,UUID.class),userId);if(!ids.isEmpty())return ids.getFirst();String name=db.queryForObject("select display_name from user_profile where id=?",String.class,userId);UUID id=UUID.randomUUID();db.update("insert into cms_author(id,user_profile_id,display_name,created_at,updated_at) values(?,?,?,current_timestamp,current_timestamp)",id,userId,name);return id;}
  private UUID authorFor(long userId){List<UUID> ids=db.query("select id from cms_author where user_profile_id=?",(rs,n)->rs.getObject(1,UUID.class),userId);if(ids.isEmpty())throw ApiException.notFound("author");return ids.getFirst();}
  private String uniqueSlug(String supplied,UUID excludingId){String base=slugs.slugify(supplied),candidate=base;for(int suffix=2;suffix<10000;suffix++){Integer count=excludingId==null?db.queryForObject("select count(*) from cms_blog_post where site_id=? and slug=?",Integer.class,DEFAULT_SITE,candidate):db.queryForObject("select count(*) from cms_blog_post where site_id=? and slug=? and id<>?",Integer.class,DEFAULT_SITE,candidate,excludingId);if(count==0)return candidate;candidate=base+"-"+suffix;}throw ApiException.conflict("Unable to create a unique slug");}
  private List<UUID> ids(String table,String column,UUID postId){return db.query("select "+column+" from "+table+" where blog_post_id=?",(rs,n)->rs.getObject(1,UUID.class),postId);}
  private void replaceTaxonomy(UUID postId,List<UUID> categories,List<UUID> tags){db.update("delete from cms_blog_post_category where blog_post_id=?",postId);db.update("delete from cms_blog_post_tag where blog_post_id=?",postId);for(UUID id:categories==null?List.<UUID>of():categories)db.update("insert into cms_blog_post_category(blog_post_id,category_id) values(?,?)",postId,id);for(UUID id:tags==null?List.<UUID>of():tags)db.update("insert into cms_blog_post_tag(blog_post_id,tag_id) values(?,?)",postId,id);}
  private List<TaxonomyView> taxonomy(String table){return db.query("select id,name,slug,description from "+allowedTaxonomy(table)+" where site_id=? order by name",(rs,n)->new TaxonomyView(rs.getObject("id",UUID.class),rs.getString("name"),rs.getString("slug"),rs.getString("description")),DEFAULT_SITE);}
  private String allowedTaxonomy(String table){if(!Set.of("cms_category","cms_tag").contains(table))throw new IllegalArgumentException("invalid taxonomy");return table;}

  record PostView(UUID id,String title,String slug,String excerpt,JsonNode content,String status,Instant publishedAt,Instant createdAt,Instant updatedAt,String authorName,List<UUID> categoryIds,List<UUID> tagIds,UUID featuredImageAssetId){}
  record TaxonomyView(UUID id,String name,String slug,String description){}
  record Page<T>(List<T> items,int page,int size,long totalElements,long totalPages){}
}
