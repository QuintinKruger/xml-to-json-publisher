## Decisions Made 
1. Use  s9api interface rather than JAXP interface 
   * Reason for this is because the docs mension the former supports XSLT 2.0 and 3.0, the later does not - [reference](https://www.saxonica.com/html/documentation12/using-xsl/embedding/index.html)
   * Assignment requirements asked specifically to use XSLT 3.0
2. DOM Tree model shall not be used 
   * XSLT version 2.0 and 3.0 have faster processing tree models that can be leveraged 
3. XML validations using JAXP 
   * validation using Saxon-HE is not possible as this is an enterprise feature only
4. XsltCompiler singleton created inspite of Javadoc mentioning error stream will be shared 
   * Its thread safe, so no worries there
   * Rather than building a new instance of compiler requiring the reprocessing of stylesheet everytime and hit on performance (micrometer test to be done to see processing time reused vs new instance - findings to be added here to motivate), accept pollution potential of errors
   * From testing, I could not see how errors could be used to report back to controller - thus decided if in future errors need to be reported back this will be relooked
5. Xslt30Transformer transformer used rather than the XsltTransformer
   * This is to leverage the XSLT 3.0 version capabilities required by the assignment
6. XSLT JIT compilation not exabled as advised for production for improved performance
   * this can only be applied on a Saxon-EE license not Saxon-HE
## References 
- [s9api](https://www.saxonica.com/html/documentation12/using-xsl/embedding/s9api-transformation.html)

## Claude Code Prompts and Responses
`prompt`: I have been tasked with an interview assessment where I should receive XML data, I must validate it against an XSD they have provided and transoform it into a specific JSON format   
(they have provided a sample format but I have some free rein here). The tech stack they have told me to use is Java 17+, Spring Boot 3, Saxon-HE (XSLT 3.0) or similar. I have some  
experience working with SOAP services but I have no idea what the Saxon-HE item provides nor do I know of any other alternative and I also dont know whether Saxon-HE is poised to    
work well with Spring boot - please can you guide me and briefly tell me how I can use Saxon-HE for this task. I plan on exposing an HTTP web service to accept the XML as documents  
to be provided
```
What Saxon-HE actually is

  Saxon-HE ("Home Edition") is the free, open-source tier of Saxonica's XML processor. It implements XSLT 3.0, XQuery 3.1, and XPath 3.1. It's a plain Java library
  (net.sf.saxon:Saxon-HE on Maven Central) — no Spring-specific glue needed, you just add it as a dependency and call its API (s9api: Processor, XsltCompiler, Xslt30Transformer) from a
  regular @Service bean. It integrates with Spring Boot exactly the way any plain Java library does — via constructor injection of a bean you configure yourself.

  One important nuance for your task: Saxon-HE does not do XSD schema validation — that's a Saxon-EE (paid) feature called "schema-aware processing." For validating XML against an XSD,
  you don't need Saxon at all — the JDK ships javax.xml.validation (SchemaFactory + Validator), which is free and sufficient for straight XSD validation.

  So the natural split for your assessment is:
  1. Validate: JAXP (javax.xml.validation) against the XSD — built into the JDK, zero extra dependencies.
  2. Transform: Saxon-HE running an XSLT 3.0 stylesheet. XSLT 3.0 has built-in functions xml-to-json() and json-to-xml(), so you can write one .xsl file that consumes the validated XML
  and emits a JSON string directly — no manual DOM walking or POJO mapping required.

  Alternatives, for context (since you asked)

  - JAXB + Jackson: generate Java classes from the XSD (jaxb2-maven-plugin), unmarshal XML into POJOs, then ObjectMapper.writeValueAsString(). More idiomatic Java, more boilerplate,
  easier to unit test field-by-field.
  - Jackson XmlMapper (jackson-dataformat-xml): direct XML→JSON in a couple of lines. Simplest, but weak control over shape/namespaces — usually not what an interviewer wants when
  they've named Saxon-HE explicitly.
  - Saxon-HE + XSLT 3.0 (what they asked for): most declarative, plays to the "SOAP/XML tooling" experience you mentioned, and is the expected answer given the stack they specified.
```

`prompt`: Please check the code up until this point - what i have done is defined the endpoint to accept multiple xml file uploads, validate them against the XSD and respond with 207 response
code. Nothing yet happens after validation - that will be my next step. Please comment on the approach taken for the code up until now - please also comment on whether 207 is the    
right response as well as the response body that is application/json type and not xml as documented here https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/207 - it  
is also mentioned on this page that 'This response is used exclusively in the context of Web Distributed Authoring and Versioning (WebDAV).' which tells me we should not response    
with this code
```
Good progress — a few solid things here and a few real issues. Going through it:

  The 207 question — you're right, drop it

  MDN's wording is accurate and worth taking seriously: 207 isn't a generic "here's a batch of results" status — it's WebDAV's status code, defined by RFC 4918, and its actual contract
  includes the body format: a text/xml/application/xml <multistatus> document with <response>/<href>/<status>/<propstat> elements. The number "207" only carries meaning in combination
  with that body shape. The moment you return application/json instead, you've kept the number but discarded everything that gives it a standard meaning — you're not really "using
  207," you're using an arbitrary integer that happens to collide with a WebDAV status, and any client/tooling/developer that looks it up will land on WebDAV docs describing a body
  format you don't produce. There's no separate spec establishing "207 for generic JSON batch APIs" — it's a colloquial reuse some APIs do, not a standard.

  Your instinct to not switch to XML instead is also right — that would mean actually adopting the WebDAV contract, which isn't warranted for a bespoke API. The cleaner move is the
  other direction: drop 207 entirely.

  Recommendation: return 200 OK with the List<FileOutcome> body as-is. This is the pattern most real batch/bulk JSON APIs actually use (Stripe, GCP, Elasticsearch's Bulk API) — the
  status code answers "did the server successfully process and report on your request," the body answers "what happened to each item." Reserve 4xx for when the server couldn't attempt
  processing at all (no file parts present, unreadable multipart body) — that's a request-level failure, not a per-item one.

  A real bug: multipartFile.getName()

  XmlToJsonPublisherController.java:31,33 — MultipartFile.getName() returns the form field name ("file", matching your @RequestParam("file")), not the uploaded filename. Since every
  file in a multi-file upload shares that same field name, every FileOutcome.fileName in your response will be the literal string "file" — you can't tell which outcome belongs to which
  upload. You want multipartFile.getOriginalFilename() instead.

A real bug: shared Validator isn't thread-safe

ValidatorConfiguration.java exposes Validator itself as a singleton bean, and the controller injects and calls .validate() on that same shared instance. JAXP's Validator is
explicitly documented as not thread-safe — concurrent calls from different threads (which will happen the moment two HTTP requests hit /upload-xml at the same time, since Tomcat
handles requests concurrently by default) can corrupt validation state or produce wrong results under the hood. Given Task 2 of your assignment explicitly wants concurrent
processing, this will bite.

Fix: Schema is thread-safe and designed to be a shared singleton — flip the bean to expose Schema, and call schema.newValidator() (cheap) each time you need to validate:
@Bean
public Schema xmlSchema() throws IOException, SAXException {
SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
return schemaFactory.newSchema(new ClassPathResource("xsd.xml").getFile());
}
private final Schema xmlSchema;
...
for (MultipartFile file : multipartFiles) {
Validator validator = xmlSchema.newValidator(); // fresh, cheap, safe per call
validator.validate(new StreamSource(file.getInputStream()));
}

ControllerAdvice: good simplification, but Exception.class is too broad

  Nice move dropping the per-file exception handling out of the advice — with validation now caught inline in the loop, the advice is correctly reduced to a last-resort net, which
  lines up with the "don't route expected per-item outcomes through exceptions" direction from before.

  But @ExceptionHandler(Exception.class) → 500 catches everything, including exceptions Spring already handles sensibly on its own — e.g. if the file request param is missing entirely,
  Spring would normally return 400 via MissingServletRequestParameterException; your catch-all now intercepts that and turns it into a 500. A missing upload is a client error, not a
  server error. Consider narrowing this (catch specific unexpected exception types, or extend ResponseEntityExceptionHandler to keep Spring's built-in 4xx mappings intact while adding
  your own handler on top for truly unhandled cases).

  Minor, not urgent

  - catch (SAXException | IOException e) lumps "content is invalid XML" together with "couldn't even read the upload" — different failure classes, probably fine to merge for now but
  worth remembering if you want more precise diagnostics later.
  - The controller now owns SAX/StreamSource plumbing directly (no more FileValidator). Reasonable for now while you're iterating, but once transform/publish gets added next, this
  method is going to keep growing — worth watching whether it's time to extract that back out into a collaborator once it does more than one thing.
```
`prompt`: Please explain to me, in the context of Saxon and XSLT what a stylesheet is and what a typical one looks like
```
What a stylesheet actually is

  An XSLT stylesheet is itself an XML document — root element <xsl:stylesheet>, in the namespace http://www.w3.org/1999/XSL/Transform (conventionally the xsl: prefix). It's not a
  script that runs top-to-bottom like Java; it's a declarative set of rules. You don't write a loop that walks the source tree — you declare template rules that say "when the processor
  encounters a node matching this pattern, produce this output," and Saxon handles the traversal, dispatching to whichever rule best matches each node it visits.

  The execution model, in brief:
  - Saxon starts at the source document's root node.
  - xsl:apply-templates tells it "go process this set of nodes" (defaults to the current node's children).
  - For each node, Saxon finds the best-matching xsl:template (by XPath pattern specificity) and runs it.
  - That template can call xsl:apply-templates again on its own descendants, recursing through the tree.

  This is a genuinely different mental model from imperative code — closer to a pattern-matching dispatch table than a program.
  
  The skeleton of a typical stylesheet

  <xsl:stylesheet version="3.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

      <xsl:output method="xml" indent="yes"/>

      <xsl:template match="/">
          <!-- entry point: matches the document node -->
          <xsl:apply-templates select="book"/>
      </xsl:template>

      <xsl:template match="book">
          <title><xsl:value-of select="@title"/></title>
      </xsl:template>

  </xsl:stylesheet>

  Core instructions worth knowing by name:
  - xsl:template match="..." — a rule, keyed by an XPath pattern.
  - xsl:apply-templates select="..." — "process these nodes by dispatching to matching templates" (the declarative/recursive way to traverse).
  - xsl:for-each select="..." — straightforward imperative-style iteration, useful when you just want to loop without full template dispatch.
  - xsl:value-of select="..." — pull a computed string into the output.
  - xsl:if / xsl:choose/xsl:when/xsl:otherwise — conditionals.
  - xsl:output method="..." — controls serialization (xml, html, text, and — the one you actually need — json).

  The JSON-specific layer (XSLT 3.0)

  This is the part that's specifically why Saxon-HE (not just any XSLT processor) was named in your brief — full XSLT 3.0 JSON serialization is a fairly recent,
  not-universally-implemented feature, and Saxon is one of the processors that actually supports it properly.

  XPath 3.1 added map and array as real data types (not XML elements — actual XDM values). XSLT 3.0 gives you xsl:map/xsl:map-entry to construct a map as the result of a template, and
  if you set <xsl:output method="json"/>, Saxon serializes that map/array value straight to JSON text — no manual string concatenation.

  <xsl:stylesheet version="3.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

      <xsl:output method="json" indent="yes"/>        
      
            <xsl:template match="/">
          <xsl:map>
              <xsl:map-entry key="'content_id'" select="judgment/header/content_id/string()"/>
              <xsl:map-entry key="'title'" select="judgment/header/title/string()"/>
              <xsl:map-entry key="'citations'">
                  <array>
                      <xsl:for-each select="judgment/header/citations/citation">
                          <xsl:map>
                              <xsl:map-entry key="'type'" select="@type/string()"/>
                              <xsl:map-entry key="'value'" select="string()"/>
                          </xsl:map>
                      </xsl:for-each>
                  </array>
              </xsl:map-entry>
          </xsl:map>
      </xsl:template>

  </xsl:stylesheet>
  That produces {"content_id": "...", "title": "...", "citations": [{"type": "...", "value": "..."}]} directly — xsl:map-entry for object keys, array { } wrapping a sequence of items
  for a JSON array, plain xsl:for-each to turn repeated elements (like citation or p) into array entries.

  (There's also an older/alternate route — build an intermediate XML tree using the <map>/<array>/<string> elements from the http://www.w3.org/2005/xpath-functions namespace and call
  the xml-to-json() function on it explicitly. Same underlying JSON vocabulary either way; xsl:map + method="json" is the more idiomatic XSLT 3.0 way to write it by hand.)

  One gotcha specific to your XML

  Your source document is namespaced (xmlns="urn:lex:content:1", elementFormDefault="qualified" in the XSD). Every match pattern in your stylesheet needs a namespace prefix bound to
  that URI, or your templates will silently match nothing:

  <xsl:stylesheet version="3.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:lex="urn:lex:content:1">

      <xsl:template match="/lex:judgment">
          ...

  Forgetting the prefix (writing match="judgment" instead of match="lex:judgme Jump to bottom (ctrl+End) ↓  XSLT bug — the pattern matches an element in no namespace, your source
elements are all in urn:lex:content:1, nothing matches, and you get an empty result with no error — worth knowing about before you spend time debugging why your output is blank.
```
`prompt`:How is the stylesheet typically provided to Saxon ? Via the requirement,, self generated from a sample XML file and an expected JSON output from the XML file ?
```
No — Saxon has no "learn the mapping from examples" capability. There's no step where you feed it a sample XML plus a sample JSON and it infers a transformation. The stylesheet is a
  plain text/XML file that you write by hand, deciding the mapping yourself by looking at the source structure (your XSD/example XML) and the target shape (the sample JSON in the
  brief) side by side — exactly the same kind of manual mapping work you'd do writing a Jackson custom serializer or a JAXB-to-DTO mapper, just expressed as XSLT template rules instead
  of Java code. The sample JSON in your assignment is a target for you (the author) to write toward — Saxon itself never sees it and has no concept of "expected output."

  How Saxon actually consumes it, mechanically

  Two separate inputs, always:
  1. The stylesheet — a .xsl file, a static resource that exists before any transformation runs.
  2. The source document — the actual XML being transformed, different every request.

  In your project, that means the stylesheet lives as a checked-in resource, same pattern as your xsd.xml:
  src/main/resources/xslt/judgment-to-json.xsl

  At runtime via s9api:
  Processor processor = new Processor(false);
  XsltCompiler compiler = processor.newXsltCompiler();
  XsltExecutable executable = compiler.compile(
      new StreamSource(new ClassPathResource("xslt/judgment-to-json.xsl").getInputStream())
  );
  Compilation happens once — parsing the .xsl, building Saxon's internal executable representation. This is the expensive part, so you'd do it at startup (a @Bean, analogous to your
  Schema bean) and hold onto the XsltExecutable.

  Then, per document:
  Xslt30Transformer transformer = executable.load30(); // cheap, do this per call
  Serializer out = processor.newSerializer(outputStream);
  transformer.applyTemplates(new StreamSource(xmlInputStream), out);
  load30()/applyTemplates() is what actually runs the rules against a given piece of source XML and produces output — this is the part that happens for every uploaded document, reusing
  the one compiled XsltExecutable.

  So the shape mirrors what you already built for XSD validation: a static Schema/XsltExecutable compiled once and held as a singleton bean, a cheap per-call object
  (Validator/Xslt30Transformer) created fresh for each document to keep things thread-safe.

```
`prompt`: Given the sample XML input and the expected JSON output, please create the judgment-to-json.xsl file for me in the resources directory (no need for  the xslt directory)
```
Confirmed — xsd.xml sits flat in src/main/resources/, so I'll match that.

● Write(src/main/resources/judgment-to-json.xsl)
  ⎿  Wrote 47 lines to src/main/resources/judgment-to-json.xsl
       1 <?xml version="1.0" encoding="UTF-8"?>
       2 <xsl:stylesheet version="3.0"
       3     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
       4     xmlns:lex="urn:lex:content:1"
       5     exclude-result-prefixes="lex">
       6 
       7     <xsl:output method="json" indent="yes"/>
       8 
       9     <xsl:template match="/lex:judgment">
      10         <xsl:variable name="paragraphs" select="lex:body/lex:section/lex:p"/>
     … +37 lines 

● Created at src/main/resources/judgment-to-json.xsl. Rundown of the mapping:

  - Namespace: xmlns:lex="urn:lex:content:1" bound and used on every match/select — matches the gotcha from earlier, since your source is namespaced.
  - Scalars (content_id, title, court, jurisdiction, decision_date): straightforward xsl:map-entry with a select pulling the string value.
  - citations/parties: built with array { for $x in ... return map {...} } — fully computed as XPath (map/array constructors + a for expression), rather than mixing in
  xsl:for-each/xsl:map instructions, which keeps each entry a single self-contained expression. Since both are minOccurs="0" in your XSD, an absent element just yields an empty
  sequence, so these normalize to [] rather than the key disappearing — noted in the comment since that's a deliberate choice, not the only valid one.
  - paragraphs: iterates $paragraphs (bound once as a variable, reused below for full_text too) — pulls id from the p's own attribute and section from parent::lex:section/@type, since
  the section type isn't on the p element itself.
  - full_text: string-join($paragraphs/string(), ' ') — same node sequence, joined in document order.

  Not wired up yet — no net.sf.saxon:Saxon-HE dependency in pom.xml, and no Xslt30Transformer code to actually run this. Given the pattern from your Schema/Validator setup, the natural
  next step would be a SaxonTransformer-style component with an XsltExecutable compiled once from this file as a singleton bean, and a cheap Xslt30Transformer created per call — happy
  to talk through that when you're ready, or you can take a first pass yourself first.

```