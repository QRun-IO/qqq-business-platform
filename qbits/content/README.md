# QBit: Content Management System (CMS)

This QBit provides a comprehensive CMS/blog for content management, supporting multi-site capabilities, static pages, articles with publication workflows, and more.

## Data Model (10 tables)

1.  **cms_site**: Multi-site capability (single site v1)
2.  **cms_page**: Static pages with SEO fields, slug uniqueness
3.  **cms_blog_post**: Articles with publication workflow
4.  **cms_author**: Content creators (bridge to user_profile)
5.  **cms_tag**: Content tagging taxonomy
6.  **cms_category**: Hierarchical content categories
7.  **cms_asset**: MinIO S3 metadata, upload tracking
8.  **cms_navigation**: Site structure, menu generation
9.  **cms_redirect**: URL management, SEO redirects
10. **cms_content_revision**: Version history, rollback capability

## Key Features

-   **Publication State Machine**: draft → scheduled → published → archived
-   **Security Locks**: `publicationState=published` for public API visibility
-   **JSON Block Editor**: Typed content blocks with JSONB validation
-   **Asset Pipeline**: Upload → MinIO → presigned URL serving
-   **SEO Optimization**: Meta tags, structured data, sitemap generation
-   **Slug Management**: Automatic generation, collision handling

## Integration Points

-   MinIO S3 for binary asset storage
-   User profile for author attribution
-   Newsletter for content promotion
-   Public API for website rendering

## Development Status

Developing...

## 1. Data Models

### 1.1. `cms_site`

Represents a single website instance.

*   `id` (PK, UUID)
*   `name` (String, unique)
*   `domain` (String, unique, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.2. `cms_page`

Represents static pages (e.g., About Us, Contact).

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `title` (String)
*   `slug` (String, unique per site)
*   `content` (JSONB - block editor content)
*   `seo_title` (String, optional)
*   `seo_description` (String, optional)
*   `meta_keywords` (String, optional)
*   `publication_state` (Enum: draft, scheduled, published, archived)
*   `published_at` (Timestamp, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.3. `cms_blog_post`

Represents articles or blog posts.

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `author_id` (FK to `cms_author`)
*   `title` (String)
*   `slug` (String, unique per site)
*   `excerpt` (Text, optional)
*   `content` (JSONB - block editor content)
*   `featured_image_asset_id` (FK to `cms_asset`, optional)
*   `seo_title` (String, optional)
*   `seo_description` (String, optional)
*   `meta_keywords` (String, optional)
*   `publication_state` (Enum: draft, scheduled, published, archived)
*   `scheduled_at` (Timestamp, optional)
*   `published_at` (Timestamp, optional)
*   `archived_at` (Timestamp, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.4. `cms_author`

Represents content creators. Links to a user profile system.

*   `id` (PK, UUID)
*   `user_profile_id` (UUID, unique, external reference)
*   `display_name` (String)
*   `bio` (Text, optional)
*   `avatar_asset_id` (FK to `cms_asset`, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.5. `cms_tag`

Represents tags for categorizing content.

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `name` (String, unique per site)
*   `slug` (String, unique per site)
*   `description` (Text, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.6. `cms_category`

Represents hierarchical categories for content.

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `parent_category_id` (FK to `cms_category`, optional, self-referencing for hierarchy)
*   `name` (String)
*   `slug` (String, unique per site)
*   `description` (Text, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.7. `cms_asset`

Stores metadata for uploaded assets (images, documents, etc.).

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `uploader_id` (FK to `cms_author`, optional)
*   `file_name` (String)
*   `file_path` (String, path in MinIO S3)
*   `file_size` (Integer, bytes)
*   `mime_type` (String)
*   `alt_text` (String, optional, for images)
*   `caption` (String, optional)
*   `upload_status` (Enum: pending, processing, completed, failed)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.8. `cms_navigation`

Defines site navigation menus and structures.

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `name` (String, e.g., "Main Navigation", "Footer Menu")
*   `items` (JSONB - structure of menu items, including labels, links, order, and hierarchy)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.9. `cms_redirect`

Manages URL redirects for SEO and URL changes.

*   `id` (PK, UUID)
*   `site_id` (FK to `cms_site`)
*   `source_url` (String, unique path)
*   `destination_url` (String)
*   `type` (Enum: 301, 302)
*   `description` (String, optional)
*   `created_at` (Timestamp)
*   `updated_at` (Timestamp)

### 1.10. `cms_content_revision`

Tracks history and allows rollbacks for content.

*   `id` (PK, UUID)
*   `content_type` (Enum: page, blog_post)
*   `content_id` (UUID, FK to `cms_page` or `cms_blog_post`)
*   `revision_number` (Integer)
*   `previous_revision_id` (FK to `cms_content_revision`, optional)
*   `user_id` (UUID, who made the revision)
*   `snapshot` (JSONB - the content at the time of revision)
*   `change_description` (String, optional)
*   `created_at` (Timestamp)

## 2. Features (Implementation Details)

### 2.1. Publication State Machine

States: `draft`, `scheduled`, `published`, `archived`.
Transitions will be managed via API endpoints or backend jobs.

### 2.2. Security Locks

Access control will ensure published content is publicly readable, while drafts/scheduled content is restricted. Details TBD based on auth system.

### 2.3. JSON Block Editor

Content fields (`content` in `cms_page`, `cms_blog_post`) will use JSONB to store structured blocks. Validation schemas will be enforced.

### 2.4. Asset Pipeline

1.  Upload request to backend.
2.  Backend generates presigned URL for MinIO.
3.  Client uploads directly to MinIO.
4.  MinIO callback/backend process updates `cms_asset` with status and metadata.

### 2.5. SEO Optimization

Fields like `seo_title`, `seo_description`, `meta_keywords` will be populated. Sitemap generation and structured data (like Schema.org) will be handled by the rendering layer or dedicated utilities.

### 2.6. Slug Management

Automatic slug generation on title change, with checks for uniqueness per `site_id`. Manual override capability.

## 3. Next Steps

1.  Refine schema definitions (e.g., add constraints, indexes).
2.  Implement database migrations for these tables.
3.  Develop API endpoints for CRUD operations on these models.
4.  Build out the asset upload pipeline with MinIO integration.
5.  Implement the publication state machine logic.
6.  Develop the JSON block editor schema and validation.
7.  Add SEO utilities and navigation generation.
