<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  
  

  


  

  <head>
    <title>
      Attachment – CUTE
    </title>
        <link rel="search" href="/cute/search" />
        <link rel="help" href="/cute/wiki/TracGuide" />
        <link rel="alternate" href="/cute/raw-attachment/wiki/FunctionalityExamples/myPrintEnv.cpp" type="text/x-c++src; charset=iso-8859-15" title="Original Format" />
        <link rel="up" href="/cute/wiki/FunctionalityExamples" title="FunctionalityExamples" />
        <link rel="start" href="/cute/wiki" />
        <link rel="stylesheet" href="/cute/chrome/common/css/trac.css" type="text/css" /><link rel="stylesheet" href="/cute/chrome/common/css/code.css" type="text/css" />
        <link rel="shortcut icon" href="/cute/chrome/common/trac.ico" type="image/x-icon" />
        <link rel="icon" href="/cute/chrome/common/trac.ico" type="image/x-icon" />
      <link type="application/opensearchdescription+xml" rel="search" href="/cute/search/opensearch" title="Search CUTE" />
    <script type="text/javascript" src="/cute/chrome/common/js/jquery.js"></script><script type="text/javascript" src="/cute/chrome/common/js/trac.js"></script><script type="text/javascript" src="/cute/chrome/common/js/search.js"></script>
    <!--[if lt IE 7]>
    <script type="text/javascript" src="/cute/chrome/common/js/ie_pre7_hacks.js"></script>
    <![endif]-->
    <link rel="stylesheet" type="text/css" href="/cute/chrome/site/style.css" />
  </head>
  <body>
	<div style="top:0px; left:300px; position:absolute">
		<img style="top:-2px;position:absolute;" src="http://r2.ifs.hsr.ch/trac/chrome/site/trac_projektlogos.png" />
		<div style="position:relative; top: 10px; left:60px;">
		<a style="position:relative; left:1em;" href="/cdtrefactoring">C++ Refactoring for CDT</a>
		<a style="position:relative; left:3em;" href="/trac">Ruby Refactoring for RDT</a>
		<a style="position:relative; left:5em;" href="/cute">Cute – C++ Unit Testing Easier</a>
		</div>
	</div>
    <div id="siteheader">
    </div>
    <div id="banner">
      <div id="header">
        <a id="logo" href="/cute"><img src="/cute/chrome/site/logo.png" alt="" /></a>
      </div>
      <form id="search" action="/cute/search" method="get">
        <div>
          <label for="proj-search">Search:</label>
          <input type="text" id="proj-search" name="q" size="18" value="" />
          <input type="submit" value="Search" />
        </div>
      </form>
      <div id="metanav" class="nav">
    <ul>
      <li class="first"><a href="/cute/login">Login</a></li><li><a href="/cute/wiki/TracGuide">Help/Guide</a></li><li><a href="/cute/about">About Trac</a></li><li class="last"><a href="/cute/prefs">Preferences</a></li>
    </ul>
  </div>
    </div>
    <div id="mainnav" class="nav">
    <ul>
      <li class="first active"><a href="/cute/wiki">Wiki</a></li><li><a href="/cute/timeline">Timeline</a></li><li><a href="/cute/roadmap">Roadmap</a></li><li><a href="/cute/report">View Tickets</a></li><li><a href="/cute/newticket">New Ticket</a></li><li class="last"><a href="/cute/search">Search</a></li>
    </ul>
  </div>
    <div id="main">
      <div id="ctxtnav" class="nav">
        <h2>Context Navigation</h2>
          <ul>
            <li class="first last"><a href="/cute/wiki/FunctionalityExamples">Back to FunctionalityExamples</a></li>
          </ul>
        <hr />
      </div>
    <div id="content" class="attachment">
        <h1><a href="/cute/wiki/FunctionalityExamples">FunctionalityExamples</a>: myPrintEnv.cpp</h1>
        <table id="info" summary="Description">
          <tbody>
            <tr>
              <th scope="col">
                File myPrintEnv.cpp, <span title="1910 bytes">1.9 kB</span>
                (added by james,  <a class="timeline" href="/cute/timeline?from=2008-04-07T11%3A27%3A05Z%2B0200&amp;precision=second" title="2008-04-07T11:27:05Z+0200 in Timeline">3 years</a> ago)
              </th>
            </tr>
            <tr>
              <td class="message searchable">
                
              </td>
            </tr>
          </tbody>
        </table>
        <div id="preview" class="searchable">
    <table class="code"><thead><tr><th class="lineno" title="Line numbers">Line</th><th class="content"> </th></tr></thead><tbody><tr><th id="L1"><a href="#L1">1</a></th><td><i><span class="code-comment">/*</span></i></td></tr><tr><th id="L2"><a href="#L2">2</a></th><td><i><span class="code-comment"> * program to print environment variables</span></i></td></tr><tr><th id="L3"><a href="#L3">3</a></th><td><i><span class="code-comment"> * invocation:</span></i></td></tr><tr><th id="L4"><a href="#L4">4</a></th><td><i><span class="code-comment"> *      prenv           print argument list</span></i></td></tr><tr><th id="L5"><a href="#L5">5</a></th><td><i><span class="code-comment"> *      prenv arg1 ...  print value of argument(s)</span></i></td></tr><tr><th id="L6"><a href="#L6">6</a></th><td><i><span class="code-comment"> * exit code: number of environment variables not found</span></i></td></tr><tr><th id="L7"><a href="#L7">7</a></th><td><i><span class="code-comment"> * author: Matt Bishop, bishop@cs.ucdavis.edu, 9/16/96</span></i></td></tr><tr><th id="L8"><a href="#L8">8</a></th><td><i><span class="code-comment"> */</span></i></td></tr><tr><th id="L9"><a href="#L9">9</a></th><td>#<b><span class="code-keyword">include</span></b> <b><span class="code-string">&lt;stdlib.h&gt;</span></b></td></tr><tr><th id="L10"><a href="#L10">10</a></th><td>#<b><span class="code-keyword">include</span></b> <b><span class="code-string">&lt;strings.h&gt;</span></b></td></tr><tr><th id="L11"><a href="#L11">11</a></th><td>#<b><span class="code-keyword">include</span></b> <b><span class="code-string">&lt;stdio.h&gt;</span></b></td></tr><tr><th id="L12"><a href="#L12">12</a></th><td></td></tr><tr><th id="L13"><a href="#L13">13</a></th><td><b><span class="code-type">int</span></b> <b><span class="code-func">main</span></b>(<b><span class="code-type">int</span></b> argc, <b><span class="code-type">char</span></b> *argv[], <b><span class="code-type">char</span></b> *envp[])</td></tr><tr><th id="L14"><a href="#L14">14</a></th><td>{</td></tr><tr><th id="L15"><a href="#L15">15</a></th><td>        <b><span class="code-type">register</span></b> <b><span class="code-type">int</span></b> i;                 <i><span class="code-comment">/* counter in for loops */</span></i></td></tr><tr><th id="L16"><a href="#L16">16</a></th><td>        <b><span class="code-type">register</span></b> <b><span class="code-type">int</span></b> narg;              <i><span class="code-comment">/* number of current argument */</span></i></td></tr><tr><th id="L17"><a href="#L17">17</a></th><td>        <b><span class="code-type">register</span></b> <b><span class="code-type">int</span></b> len;               <i><span class="code-comment">/* length of current argument */</span></i></td></tr><tr><th id="L18"><a href="#L18">18</a></th><td>        <b><span class="code-type">register</span></b> <b><span class="code-type">int</span></b> found;             <i><span class="code-comment">/* 1 if env. variable found */</span></i></td></tr><tr><th id="L19"><a href="#L19">19</a></th><td>        <b><span class="code-type">register</span></b> <b><span class="code-type">int</span></b> exstat = 0;        <i><span class="code-comment">/* exit status code */</span></i></td></tr><tr><th id="L20"><a href="#L20">20</a></th><td></td></tr><tr><th id="L21"><a href="#L21">21</a></th><td>        <i><span class="code-comment">/*</span></i></td></tr><tr><th id="L22"><a href="#L22">22</a></th><td><i><span class="code-comment">         * no arguments; just print the environment</span></i></td></tr><tr><th id="L23"><a href="#L23">23</a></th><td><i><span class="code-comment">         * variables and their values</span></i></td></tr><tr><th id="L24"><a href="#L24">24</a></th><td><i><span class="code-comment">         */</span></i></td></tr><tr><th id="L25"><a href="#L25">25</a></th><td>        <b><span class="code-lang">if</span></b> (argc == 1){</td></tr><tr><th id="L26"><a href="#L26">26</a></th><td>                <i><span class="code-comment">/* just loop and print */</span></i></td></tr><tr><th id="L27"><a href="#L27">27</a></th><td>                <b><span class="code-lang">for</span></b>(i = 0; envp[i] != NULL; i++)</td></tr><tr><th id="L28"><a href="#L28">28</a></th><td>                        printf(<b><span class="code-string">"%s\n"</span></b>, envp[i]);</td></tr><tr><th id="L29"><a href="#L29">29</a></th><td>                <i><span class="code-comment">/* all done! */</span></i></td></tr><tr><th id="L30"><a href="#L30">30</a></th><td>                <b><span class="code-lang">return</span></b>(EXIT_SUCCESS);</td></tr><tr><th id="L31"><a href="#L31">31</a></th><td>        }</td></tr><tr><th id="L32"><a href="#L32">32</a></th><td></td></tr><tr><th id="L33"><a href="#L33">33</a></th><td>        <i><span class="code-comment">/*</span></i></td></tr><tr><th id="L34"><a href="#L34">34</a></th><td><i><span class="code-comment">         * arguments given; just print the values</span></i></td></tr><tr><th id="L35"><a href="#L35">35</a></th><td><i><span class="code-comment">         * associated with these named variables</span></i></td></tr><tr><th id="L36"><a href="#L36">36</a></th><td><i><span class="code-comment">         */</span></i></td></tr><tr><th id="L37"><a href="#L37">37</a></th><td>        <b><span class="code-lang">for</span></b>(narg = 1; argv[narg] != NULL; narg++){</td></tr><tr><th id="L38"><a href="#L38">38</a></th><td>                <i><span class="code-comment">/* just an optimization ... */</span></i></td></tr><tr><th id="L39"><a href="#L39">39</a></th><td>                len = strlen(argv[narg]);</td></tr><tr><th id="L40"><a href="#L40">40</a></th><td>                <i><span class="code-comment">/* assume no such variable */</span></i></td></tr><tr><th id="L41"><a href="#L41">41</a></th><td>                found = 0;</td></tr><tr><th id="L42"><a href="#L42">42</a></th><td>                <i><span class="code-comment">/*</span></i></td></tr><tr><th id="L43"><a href="#L43">43</a></th><td><i><span class="code-comment">                 * now look for the variable</span></i></td></tr><tr><th id="L44"><a href="#L44">44</a></th><td><i><span class="code-comment">                 */</span></i></td></tr><tr><th id="L45"><a href="#L45">45</a></th><td>                <b><span class="code-lang">for</span></b>(i = 0; envp[i] != NULL; i++)</td></tr><tr><th id="L46"><a href="#L46">46</a></th><td>                        <i><span class="code-comment">/* see if this one is it*/</span></i></td></tr><tr><th id="L47"><a href="#L47">47</a></th><td>                        <b><span class="code-lang">if</span></b> (strncmp(envp[i], argv[narg], len) == 0){</td></tr><tr><th id="L48"><a href="#L48">48</a></th><td>                                <i><span class="code-comment">/* name= means value follows = */</span></i></td></tr><tr><th id="L49"><a href="#L49">49</a></th><td>                                <b><span class="code-lang">if</span></b> (envp[i][len] == <b><span class="code-string">'='</span></b>){</td></tr><tr><th id="L50"><a href="#L50">50</a></th><td>                                        printf(<b><span class="code-string">"%s\n"</span></b>, envp[i] + len + 1);</td></tr><tr><th id="L51"><a href="#L51">51</a></th><td>                                        found++;</td></tr><tr><th id="L52"><a href="#L52">52</a></th><td>                                }</td></tr><tr><th id="L53"><a href="#L53">53</a></th><td>                                <i><span class="code-comment">/* name means value is empty */</span></i></td></tr><tr><th id="L54"><a href="#L54">54</a></th><td>                                <i><span class="code-comment">/* anything else, it doesn't match */</span></i></td></tr><tr><th id="L55"><a href="#L55">55</a></th><td>                                <b><span class="code-lang">else</span></b> <b><span class="code-lang">if</span></b> (!envp[i][len]){</td></tr><tr><th id="L56"><a href="#L56">56</a></th><td>                                        putchar(<b><span class="code-string">'\n'</span></b>);</td></tr><tr><th id="L57"><a href="#L57">57</a></th><td>                                        found++;</td></tr><tr><th id="L58"><a href="#L58">58</a></th><td>                                }</td></tr><tr><th id="L59"><a href="#L59">59</a></th><td>                        }</td></tr><tr><th id="L60"><a href="#L60">60</a></th><td>                <i><span class="code-comment">/*</span></i></td></tr><tr><th id="L61"><a href="#L61">61</a></th><td><i><span class="code-comment">                 * did we find it?</span></i></td></tr><tr><th id="L62"><a href="#L62">62</a></th><td><i><span class="code-comment">                 */</span></i></td></tr><tr><th id="L63"><a href="#L63">63</a></th><td>                <b><span class="code-lang">if</span></b> (!found){</td></tr><tr><th id="L64"><a href="#L64">64</a></th><td>                        <i><span class="code-comment">/* nope -- print error message */</span></i></td></tr><tr><th id="L65"><a href="#L65">65</a></th><td>                        fprintf(stderr, <b><span class="code-string">"%s: no such variable\n"</span></b>,</td></tr><tr><th id="L66"><a href="#L66">66</a></th><td>                                                                   argv[narg]);</td></tr><tr><th id="L67"><a href="#L67">67</a></th><td>                        <i><span class="code-comment">/* one more unknown environment variable */</span></i></td></tr><tr><th id="L68"><a href="#L68">68</a></th><td>                        exstat++;</td></tr><tr><th id="L69"><a href="#L69">69</a></th><td>                }</td></tr><tr><th id="L70"><a href="#L70">70</a></th><td></td></tr><tr><th id="L71"><a href="#L71">71</a></th><td>        }</td></tr><tr><th id="L72"><a href="#L72">72</a></th><td></td></tr><tr><th id="L73"><a href="#L73">73</a></th><td>        <i><span class="code-comment">/*</span></i></td></tr><tr><th id="L74"><a href="#L74">74</a></th><td><i><span class="code-comment">         * return number of unknown environment variables</span></i></td></tr><tr><th id="L75"><a href="#L75">75</a></th><td><i><span class="code-comment">         */</span></i></td></tr><tr><th id="L76"><a href="#L76">76</a></th><td>        <b><span class="code-lang">return</span></b>(exstat);</td></tr><tr><th id="L77"><a href="#L77">77</a></th><td>}</td></tr></tbody></table>
        </div>
    </div>
    <div id="altlinks">
      <h3>Download in other formats:</h3>
      <ul>
        <li class="last first">
          <a rel="nofollow" href="/cute/raw-attachment/wiki/FunctionalityExamples/myPrintEnv.cpp">Original Format</a>
        </li>
      </ul>
    </div>
    </div>
    <div id="footer" lang="en" xml:lang="en"><hr />
      <a id="tracpowered" href="http://trac.edgewall.org/"><img src="/cute/chrome/common/trac_logo_mini.png" height="30" width="107" alt="Trac Powered" /></a>
      <p class="left">
        Powered by <a href="/cute/about"><strong>Trac 0.11stable-r7581</strong></a><br />
        By <a href="http://www.edgewall.org/">Edgewall Software</a>.
      </p>
      <p class="right">Visit the Trac open source project at<br /><a href="http://trac.edgewall.org/">http://trac.edgewall.org/</a></p>
    </div>
    <div id="sitefooter">
    </div>
  </body>
</html>