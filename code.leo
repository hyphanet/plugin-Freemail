<?xml version="1.0" encoding="UTF-8"?>
<leo_file>
<leo_header file_format="2" tnodes="0" max_tnode_index="494" clone_windows="0"/>
<globals body_outline_ratio="0.29999999999999999">
	<global_window_position top="39" left="131" height="647" width="1104"/>
	<global_log_window_position top="0" left="0" height="0" width="0"/>
</globals>
<preferences allow_rich_text="0">
</preferences>
<find_panel_settings>
	<find_string></find_string>
	<change_string></change_string>
</find_panel_settings>
<vnodes>
<v t="T1" a="E"><vh>Freemail</vh>
<v t="T2" a="E" tnodeList="2,3,4,5"><vh>@file designNotes.txt</vh>
<v t="T3"><vh>general</vh></v>
<v t="T4"><vh>simplification</vh></v>
<v t="T5" a="M"><vh>mailing lists</vh></v>
</v>
<v t="T6"><vh>TODO</vh>
<v t="T7" a="E"><vh>test on windows</vh>
<v t="T8"><vh>build windows standalone</vh></v>
<v t="T9"><vh>build windows installer EXE</vh></v>
</v>
<v t="T10" a="E"><vh>test on virgin linux</vh>
<v t="T11" a="M"><vh>try freezing on virgin linux</vh></v>
</v>
<v t="T12" a="E"><vh>documentation</vh>
<v t="T13"><vh>complete website pages</vh></v>
<v t="T14"><vh>complete the manual</vh></v>
</v>
<v t="T15" a="E"><vh>testing</vh>
<v t="T16"><vh>test with multi-threading</vh></v>
<v t="T17" a="E"><vh>torture-testing</vh>
<v t="T18" a="M"><vh>add 'sabotage' method to freenet</vh></v>
<v t="T19" a="M"><vh>test at various sabotage levels</vh></v>
</v>
</v>
<v t="T20" a="E"><vh>Packaging</vh></v>
<v t="T21" a="E"><vh>misc</vh>
<v t="T22" a="M"><vh>write console-based standalone db util</vh></v>
<v t="T23" a="E"><vh>SSLCrypto</vh>
<v t="T24"><vh>replace pickle with better serialiser</vh></v>
</v>
<v t="T25" a="E"><vh>security</vh>
<v t="T26" a="M"><vh>Change pyweb hmac string</vh></v>
</v>
</v>
</v>
<v t="T27" a="E" tnodeList="27,28,29,30,31,32,33,35,36,37,38,39,40,41,44,45,46,48,50,52,55,56,58,59,61,62,64,67,69,70,71,72,74,75,76,78,79,80,81,82,83,84,85,87,88,89,90,91,92,93,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,113,114,115,117,118,119,120,123,124,125,126,128,129,130,131,132,133,134,135,137,138,139,141,142,143,144,145,146,147,148,149,150,151,152,153,154,156,157,158,159,160,161,162,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,223,224,225,226,227,228,229,230,232,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,272,273,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,292"><vh>@file freemail.py</vh>
<v t="T28"><vh>Suppress old API warnings</vh></v>
<v t="T29"><vh>imports</vh></v>
<v t="T30"><vh>globals</vh></v>
<v t="T31"><vh>exceptions</vh></v>
<v t="T32" a="E"><vh>class freemailServer</vh>
<v t="T33"><vh>__init__</vh></v>
<v t="T34" a="E"><vh>Main Methods</vh>
<v t="T35"><vh>runServer</vh></v>
<v t="T36"><vh>startServer</vh></v>
<v t="T37"><vh>startMailServers</vh></v>
<v t="T38"><vh>startWebServer</vh></v>
<v t="T39"><vh>startTelnetServer</vh></v>
<v t="T40"><vh>startFcpEngine</vh></v>
<v t="T41"><vh>stopServer</vh></v>
<v t="T42" a="E"><vh>Client Interface</vh>
<v t="T43"><vh>POP Server methods</vh>
<v t="T44"><vh>threadPopServer</vh></v>
<v t="T45"><vh>check_user</vh></v>
<v t="T46"><vh>auth</vh></v>
</v>
<v t="T47"><vh>SMTP Server methods</vh>
<v t="T48"><vh>threadSmtpServer</vh></v>
</v>
<v t="T49" a="E"><vh>HTTP Server methods</vh>
<v t="T50"><vh>threadHttpServer</vh></v>
</v>
<v t="T51" a="E"><vh>Telnet Server methods</vh>
<v t="T52"><vh>threadTelnetServer</vh></v>
</v>
</v>
</v>
<v t="T53" a="E"><vh>FCP Engine Methods</vh>
<v t="T54" a="E"><vh>Main Engine Logic</vh>
<v t="T55"><vh>threadFcpEngine</vh></v>
<v t="T56"><vh>chugFcpEngine</vh></v>
</v>
<v t="T57" a="E"><vh>Mailsite Insertion</vh>
<v t="T58"><vh>threadFcpRefreshMailsite</vh></v>
<v t="T59"><vh>fcpRefreshMailsite</vh></v>
</v>
<v t="T60" a="EM"><vh>List Insertion</vh>
<v t="T61" a="M"><vh>threadFcpRefreshList</vh></v>
<v t="T62" a="M"><vh>fcpRefreshList</vh></v>
</v>
<v t="T63" a="E"><vh>Mailsite Retrieval</vh>
<v t="T64"><vh>fcpGetPeerInfo</vh></v>
<v t="T65"><vh>_fcpGetPeerInfo</vh></v>
</v>
<v t="T66" a="EM"><vh>Listsite Retrieval</vh>
<v t="T67" a="M"><vh>fcpGetListInfo</vh></v>
</v>
<v t="T68" a="E"><vh>Mail Receive</vh>
<v t="T69"><vh>threadFcpPollSlot</vh></v>
<v t="T70"><vh>chugFcpPollSlot</vh></v>
<v t="T71"><vh>fcpGetMsgFromSlot</vh></v>
<v t="T72"><vh>fcpParseMailMsg</vh></v>
</v>
<v t="T73" a="E"><vh>Mail Send</vh>
<v t="T74"><vh>threadFcpDeliverMsg</vh></v>
<v t="T75"><vh>chugFcpDeliverMsg</vh></v>
<v t="T76"><vh>fcpTransmitMessage</vh></v>
</v>
</v>
<v t="T77" a="E"><vh>Postmaster Messages</vh>
<v t="T78"><vh>pmMsgDeletedFromOutbox</vh></v>
<v t="T79"><vh>pmMsgDeletedFromInbox</vh></v>
<v t="T80"><vh>pmNoPeerInfo</vh></v>
<v t="T81"><vh>pmMsgRouteNotFound</vh></v>
<v t="T82"><vh>pmReceiptRouteNotFound</vh></v>
<v t="T83"><vh>pmMsgFreenetException</vh></v>
<v t="T84"><vh>pmReceiptFreenetException</vh></v>
<v t="T85"><vh>pmMaxedRetries</vh></v>
</v>
<v t="T86" a="E"><vh>Mailing Methods</vh>
<v t="T87"><vh>enqueueMessage</vh></v>
<v t="T88"><vh>writeToInbox</vh></v>
<v t="T89"><vh>readFromOutbox</vh></v>
<v t="T90"><vh>readFromInbox</vh></v>
<v t="T91"><vh>writeToOutbox</vh></v>
<v t="T92"><vh>writeToInboxRaw</vh></v>
<v t="T93"><vh>writeFromPostmaster</vh></v>
</v>
<v t="T94" a="E"><vh>Database Methods</vh>
<v t="T95"><vh>_dbLock</vh></v>
<v t="T96"><vh>_dbUnlock</vh></v>
<v t="T97"><vh>dbLoad</vh></v>
<v t="T98"><vh>dbUpdate</vh></v>
<v t="T99"><vh>dbIntegrityOk</vh></v>
<v t="T100"><vh>dbInit</vh></v>
<v t="T101"><vh>dbSave</vh></v>
<v t="T102"><vh>dbCleanup</vh></v>
<v t="T103"><vh>dbAddIdentity</vh></v>
<v t="T104" a="M"><vh>dbAddList</vh></v>
<v t="T105"><vh>dbDelIdentity</vh></v>
<v t="T106"><vh>dbAddPeer</vh></v>
<v t="T107"><vh>dbDelPeer</vh></v>
<v t="T108"><vh>dbDelTxMsg</vh></v>
<v t="T109"><vh>dbDelRxMsg</vh></v>
<v t="T110"><vh>dbGetTxMsgBody</vh></v>
<v t="T111"><vh>dbGetRxMsgBody</vh></v>
</v>
<v t="T112" a="E"><vh>Logging Methods</vh>
<v t="T113"><vh>log</vh></v>
<v t="T114"><vh>logFreenet</vh></v>
<v t="T115"><vh>verbosity</vh></v>
</v>
<v t="T116"><vh>Utility Methods</vh>
<v t="T117"><vh>forceIdRefresh</vh></v>
<v t="T118"><vh>Node</vh></v>
<v t="T119"><vh>decryptAndVerify</vh></v>
<v t="T120"><vh>signAndEncrypt</vh></v>
</v>
</v>
<v t="T121" a="E"><vh>User Interface Classes</vh>
<v t="T122" a="E"><vh>HTTP Server</vh>
<v t="T123" a="E"><vh>class WebUI</vh>
<v t="T124"><vh>__init__</vh></v>
<v t="T125"><vh>run</vh></v>
<v t="T126"><vh>setCSS</vh></v>
<v t="T127" a="E"><vh>Display Pages</vh>
<v t="T128"><vh>page_main</vh></v>
<v t="T129"><vh>page_configureSystem</vh></v>
<v t="T130"><vh>page_manageIdentities</vh></v>
<v t="T131"><vh>page_createIdentity</vh></v>
<v t="T132" a="M"><vh>page_createList</vh></v>
<v t="T133"><vh>page_manageInbound</vh></v>
<v t="T134"><vh>page_manageOutbound</vh></v>
<v t="T135"><vh>page_template</vh></v>
</v>
<v t="T136" a="E"><vh>Utilities</vh>
<v t="T137"><vh>envDump</vh></v>
<v t="T138"><vh>isLoggedIn</vh></v>
<v t="T139"><vh>showLoginPage</vh></v>
</v>
<v t="T140" a="E"><vh>Setup Wizard</vh>
<v t="T141"><vh>wizard</vh></v>
<v t="T142"><vh>wizardSetState</vh></v>
<v t="T143"><vh>wizardNextButton</vh></v>
<v t="T144"><vh>wizard_new</vh></v>
<v t="T145"><vh>wizard_security</vh></v>
<v t="T146"><vh>wizard_fcp</vh></v>
<v t="T147"><vh>wizard_servers</vh></v>
<v t="T148"><vh>wizard_misc</vh></v>
<v t="T149"><vh>wizard_finish</vh></v>
</v>
</v>
<v t="T150"><vh>class HTTPServer</vh>
<v t="T151"><vh>attribs</vh></v>
<v t="T152"><vh>__init__</vh></v>
<v t="T153"><vh>run</vh></v>
</v>
<v t="T154"><vh>class HTTPRequestHandler</vh>
<v t="T155" a="M"><vh>attributes</vh></v>
<v t="T156"><vh>__init__</vh></v>
<v t="T157"><vh>do_GET</vh></v>
<v t="T158"><vh>do_POST</vh></v>
<v t="T159"><vh>do_HEAD</vh></v>
<v t="T160"><vh>run_cgi</vh></v>
<v t="T161"><vh>translate_path</vh></v>
<v t="T162"><vh>log_message</vh></v>
</v>
</v>
<v t="T163" a="E"><vh>POP Server</vh>
<v t="T164" a="E"><vh>class POPserver</vh>
<v t="T165"><vh>__init__</vh></v>
<v t="T166"><vh>exitNormal</vh></v>
<v t="T167"><vh>run</vh></v>
</v>
<v t="T168" a="E"><vh>class POPRequestHandler</vh>
<v t="T169"><vh>__init__</vh></v>
<v t="T170"><vh>attributes</vh></v>
<v t="T171"><vh>handle</vh></v>
</v>
<v t="T172" a="E"><vh>class POPSession</vh>
<v t="T173"><vh>attributes</vh></v>
<v t="T174"><vh>__init__</vh></v>
<v t="T175"><vh>write_ok</vh></v>
<v t="T176"><vh>write_multi</vh></v>
<v t="T177"><vh>write_raw</vh></v>
<v t="T178"><vh>__check_state</vh></v>
<v t="T179"><vh>CAPA</vh></v>
<v t="T180"><vh>QUIT</vh></v>
<v t="T181"><vh>USER</vh></v>
<v t="T182"><vh>PASS</vh></v>
<v t="T183"><vh>STAT</vh></v>
<v t="T184"><vh>LIST</vh></v>
<v t="T185"><vh>RETR</vh></v>
<v t="T186"><vh>DELE</vh></v>
<v t="T187"><vh>NOOP</vh></v>
<v t="T188"><vh>RSET</vh></v>
<v t="T189"><vh>TOP</vh></v>
<v t="T190"><vh>UIDL</vh></v>
</v>
<v t="T191" a="E"><vh>class POPMaildrop</vh>
<v t="T192"><vh>__init__</vh></v>
<v t="T193"><vh>get_stat</vh></v>
<v t="T194"><vh>get_msg</vh></v>
<v t="T195"><vh>delete_msg</vh></v>
<v t="T196"><vh>update</vh></v>
<v t="T197"><vh>reset</vh></v>
</v>
<v t="T198"><vh>POP3Logger</vh></v>
<v t="T199"><vh>mydebug</vh></v>
<v t="T200"><vh>class Error</vh></v>
<v t="T201"><vh>_digest</vh></v>
<v t="T202"><vh>_get_no</vh></v>
</v>
<v t="T203"><vh>SMTP Server</vh>
<v t="T204" a="E"><vh>class SMTPServer</vh>
<v t="T205"><vh>__init__</vh></v>
<v t="T206"><vh>run</vh></v>
</v>
<v t="T207" a="E"><vh>class SMTPSession</vh>
<v t="T208"><vh>&lt;&lt; class SMTPSession declarations &gt;&gt;</vh></v>
<v t="T209"><vh>__init__</vh></v>
<v t="T210"><vh>chug</vh></v>
<v t="T211"><vh>doCommand</vh></v>
<v t="T212"><vh>doData</vh></v>
<v t="T213"><vh>logStatus</vh></v>
<v t="T214"><vh>logException</vh></v>
<v t="T215"><vh>helo</vh></v>
<v t="T216"><vh>mailFrom</vh></v>
<v t="T217"><vh>rcptTo</vh></v>
<v t="T218"><vh>data</vh></v>
<v t="T219"><vh>quit</vh></v>
<v t="T220"><vh>reset</vh></v>
<v t="T221"><vh>dispatchMessage</vh></v>
</v>
</v>
<v t="T222"><vh>Telnet Server</vh>
<v t="T223" a="E"><vh>class TelnetServer</vh>
<v t="T224"><vh>__init__</vh></v>
<v t="T225"><vh>exitNormal</vh></v>
<v t="T226"><vh>run</vh></v>
</v>
<v t="T227" a="E"><vh>class TelnetRequestHandler</vh>
<v t="T228"><vh>__init__</vh></v>
<v t="T229"><vh>handle</vh></v>
<v t="T230"><vh>getline</vh></v>
</v>
</v>
</v>
<v t="T231" a="E"><vh>Interactive Console</vh>
<v t="T232" a="C"><vh>class freemailConsole</vh></v>
</v>
<v t="T233" a="E"><vh>Utility Classes</vh>
<v t="T234" a="C"><vh>class cell</vh>
<v t="T235"><vh>__init__</vh></v>
<v t="T236"><vh>__getattr__</vh></v>
<v t="T237"><vh>__setattr__</vh></v>
<v t="T238"><vh>__delattr__</vh></v>
<v t="T239"><vh>__iter__</vh></v>
<v t="T240"><vh>__delattr__</vh></v>
<v t="T241"><vh>__getitem__</vh></v>
<v t="T242"><vh>__setitem__</vh></v>
<v t="T243"><vh>__delitem__</vh></v>
<v t="T244"><vh>__getslice__</vh></v>
<v t="T245"><vh>__setslice__</vh></v>
<v t="T246"><vh>__nonzero__</vh></v>
<v t="T247"><vh>__len__</vh></v>
<v t="T248"><vh>__str__</vh></v>
<v t="T249"><vh>__repr__</vh></v>
<v t="T250"><vh>append</vh></v>
<v t="T251"><vh>dump</vh></v>
<v t="T252"><vh>dumpseq</vh></v>
<v t="T253"><vh>extend</vh></v>
<v t="T254"><vh>getColAsList</vh></v>
<v t="T255"><vh>has_key</vh></v>
<v t="T256"><vh>index</vh></v>
<v t="T257"><vh>insert</vh></v>
<v t="T258"><vh>items</vh></v>
<v t="T259"><vh>join</vh></v>
<v t="T260"><vh>keys</vh></v>
<v t="T261"><vh>onlyWhen</vh></v>
<v t="T262"><vh>pop</vh></v>
<v t="T263"><vh>project</vh></v>
<v t="T264"><vh>remove</vh></v>
<v t="T265"><vh>removeWhen</vh></v>
<v t="T266"><vh>reverse</vh></v>
<v t="T267"><vh>select</vh></v>
<v t="T268"><vh>setDefaults</vh></v>
<v t="T269"><vh>sort</vh></v>
<v t="T270"><vh>unique</vh></v>
<v t="T271"><vh>values</vh></v>
</v>
<v t="T272" a="C"><vh>class slotmap</vh></v>
<v t="T273"><vh>DynamicSemaphore</vh></v>
</v>
<v t="T274" a="E"><vh>Global Functions</vh>
<v t="T275"><vh>b64enc</vh></v>
<v t="T276"><vh>dhms</vh></v>
<v t="T277"><vh>dbrStartTime</vh></v>
<v t="T278"><vh>exceptionString</vh></v>
<v t="T279"><vh>stackString</vh></v>
<v t="T280"><vh>stripAddress</vh></v>
<v t="T281"><vh>hash</vh></v>
<v t="T282"><vh>randstring</vh></v>
<v t="T283"><vh>getNewTid</vh></v>
<v t="T284"><vh>ulFromList</vh></v>
<v t="T285"><vh>freemailAddrToUri</vh></v>
<v t="T286"><vh>uriToFreemailAddr</vh></v>
<v t="T287"><vh>btnForm</vh></v>
<v t="T288"><vh>btn</vh></v>
<v t="T289"><vh>usage</vh></v>
<v t="T290"><vh>help</vh></v>
<v t="T291"><vh>main</vh></v>
</v>
<v t="T292"><vh>MAINLINE</vh></v>
</v>
<v t="T293" a="E"><vh>Package Files</vh>
<v t="T294"><vh>@file start-freemail.sh</vh></v>
<v t="T295"><vh>@file stop-freemail.sh</vh></v>
<v t="T296" a="E" tnodeList="296,297,232,298"><vh>@file console.py</vh>
<v t="T297"><vh>imports</vh></v>
<v t="T232" a="C"><vh>class freemailConsole</vh></v>
<v t="T298"><vh>mainline</vh></v>
</v>
<v t="T299"><vh>@file Makefile</vh></v>
<v t="T300"><vh>@file README</vh></v>
<v t="T301"><vh>@file INSTALL</vh></v>
<v t="T302"><vh>@file LICENSE</vh></v>
<v t="T303" a="MV" tnodeList="303"><vh>@file CHANGELOG</vh></v>
<v t="T304"><vh>@file ROADMAP</vh></v>
</v>
<v t="T305" a="E"><vh>Development Files</vh>
<v t="T306"><vh>@file autoexec.py</vh></v>
<v t="T307"><vh>@file tst1.py</vh></v>
<v t="T308"><vh>@file tst2.py</vh></v>
<v t="T309" a="E" tnodeList="309,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,310"><vh>@file tstcell.py</vh>
<v t="T234" a="C"><vh>class cell</vh>
<v t="T235"><vh>__init__</vh></v>
<v t="T236"><vh>__getattr__</vh></v>
<v t="T237"><vh>__setattr__</vh></v>
<v t="T238"><vh>__delattr__</vh></v>
<v t="T239"><vh>__iter__</vh></v>
<v t="T240"><vh>__delattr__</vh></v>
<v t="T241"><vh>__getitem__</vh></v>
<v t="T242"><vh>__setitem__</vh></v>
<v t="T243"><vh>__delitem__</vh></v>
<v t="T244"><vh>__getslice__</vh></v>
<v t="T245"><vh>__setslice__</vh></v>
<v t="T246"><vh>__nonzero__</vh></v>
<v t="T247"><vh>__len__</vh></v>
<v t="T248"><vh>__str__</vh></v>
<v t="T249"><vh>__repr__</vh></v>
<v t="T250"><vh>append</vh></v>
<v t="T251"><vh>dump</vh></v>
<v t="T252"><vh>dumpseq</vh></v>
<v t="T253"><vh>extend</vh></v>
<v t="T254"><vh>getColAsList</vh></v>
<v t="T255"><vh>has_key</vh></v>
<v t="T256"><vh>index</vh></v>
<v t="T257"><vh>insert</vh></v>
<v t="T258"><vh>items</vh></v>
<v t="T259"><vh>join</vh></v>
<v t="T260"><vh>keys</vh></v>
<v t="T261"><vh>onlyWhen</vh></v>
<v t="T262"><vh>pop</vh></v>
<v t="T263"><vh>project</vh></v>
<v t="T264"><vh>remove</vh></v>
<v t="T265"><vh>removeWhen</vh></v>
<v t="T266"><vh>reverse</vh></v>
<v t="T267"><vh>select</vh></v>
<v t="T268"><vh>setDefaults</vh></v>
<v t="T269"><vh>sort</vh></v>
<v t="T270"><vh>unique</vh></v>
<v t="T271"><vh>values</vh></v>
</v>
<v t="T310"><vh>test code</vh></v>
</v>
<v t="T311" a="E" tnodeList="311,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,312,313,314,315,316,317,318,319,320,321,322"><vh>@file cell.py</vh>
<v t="T234" a="CE"><vh>class cell</vh>
<v t="T235"><vh>__init__</vh></v>
<v t="T236"><vh>__getattr__</vh></v>
<v t="T237"><vh>__setattr__</vh></v>
<v t="T238"><vh>__delattr__</vh></v>
<v t="T239"><vh>__iter__</vh></v>
<v t="T240"><vh>__delattr__</vh></v>
<v t="T241"><vh>__getitem__</vh></v>
<v t="T242"><vh>__setitem__</vh></v>
<v t="T243"><vh>__delitem__</vh></v>
<v t="T244"><vh>__getslice__</vh></v>
<v t="T245"><vh>__setslice__</vh></v>
<v t="T246"><vh>__nonzero__</vh></v>
<v t="T247"><vh>__len__</vh></v>
<v t="T248"><vh>__str__</vh></v>
<v t="T249"><vh>__repr__</vh></v>
<v t="T250"><vh>append</vh></v>
<v t="T251"><vh>dump</vh></v>
<v t="T252"><vh>dumpseq</vh></v>
<v t="T253"><vh>extend</vh></v>
<v t="T254"><vh>getColAsList</vh></v>
<v t="T255"><vh>has_key</vh></v>
<v t="T256"><vh>index</vh></v>
<v t="T257"><vh>insert</vh></v>
<v t="T258"><vh>items</vh></v>
<v t="T259"><vh>join</vh></v>
<v t="T260"><vh>keys</vh></v>
<v t="T261"><vh>onlyWhen</vh></v>
<v t="T262"><vh>pop</vh></v>
<v t="T263"><vh>project</vh></v>
<v t="T264"><vh>remove</vh></v>
<v t="T265"><vh>removeWhen</vh></v>
<v t="T266"><vh>reverse</vh></v>
<v t="T267"><vh>select</vh></v>
<v t="T268"><vh>setDefaults</vh></v>
<v t="T269"><vh>sort</vh></v>
<v t="T270"><vh>unique</vh></v>
<v t="T271"><vh>values</vh></v>
</v>
<v t="T312" a="E"><vh>Bencoder functions</vh>
<v t="T313"><vh>imports</vh></v>
<v t="T314"><vh>global data</vh></v>
<v t="T315"><vh>decode_int</vh></v>
<v t="T316"><vh>decode_string</vh></v>
<v t="T317"><vh>decode_list</vh></v>
<v t="T318"><vh>decode_dict</vh></v>
<v t="T319"><vh>bdecode_rec</vh></v>
<v t="T320"><vh>bdecode</vh></v>
<v t="T321"><vh>bencode_rec</vh></v>
<v t="T322"><vh>bencode</vh></v>
</v>
</v>
<v t="T323" a="E"><vh>@file slot.py</vh>
<v t="T272" a="C"><vh>class slotmap</vh></v>
<v t="T324"><vh>mainline</vh></v>
</v>
<v t="T325"><vh>@file tst_tx.py</vh></v>
<v t="T326"><vh>@file tst_rx.py</vh></v>
<v t="T327"><vh>@file pshell.py</vh></v>
<v t="T328"><vh>@file Makefile</vh></v>
<v t="T329" a="E" tnodeList="329"><vh>@file release.py</vh></v>
<v t="T330"><vh>@file setup.py</vh></v>
<v t="T331"><vh>@file setupwin.py</vh></v>
<v t="T332" a="E"><vh>@file hackdb.py</vh></v>
</v>
<v t="T333"><vh>old stuff</vh>
<v t="T334" a="E"><vh>POP Server</vh>
<v t="T335" a="E"><vh>class POPserver</vh>
<v t="T336"><vh>__init__</vh></v>
<v t="T337"><vh>exitNormal</vh></v>
<v t="T338"><vh>run</vh></v>
</v>
<v t="T339" a="E"><vh>class POPRequestHandler</vh>
<v t="T340"><vh>__init__</vh></v>
<v t="T341"><vh>attributes</vh></v>
<v t="T342"><vh>handle</vh></v>
</v>
<v t="T343" a="E"><vh>class POPSession</vh>
<v t="T344"><vh>attributes</vh></v>
<v t="T345"><vh>__init__</vh></v>
<v t="T346"><vh>write_ok</vh></v>
<v t="T347"><vh>write_multi</vh></v>
<v t="T348"><vh>write_raw</vh></v>
<v t="T349"><vh>__check_state</vh></v>
<v t="T350"><vh>CAPA</vh></v>
<v t="T351"><vh>QUIT</vh></v>
<v t="T352"><vh>USER</vh></v>
<v t="T353"><vh>PASS</vh></v>
<v t="T354"><vh>STAT</vh></v>
<v t="T355"><vh>LIST</vh></v>
<v t="T356"><vh>RETR</vh></v>
<v t="T357"><vh>DELE</vh></v>
<v t="T358"><vh>NOOP</vh></v>
<v t="T359"><vh>RSET</vh></v>
<v t="T360"><vh>TOP</vh></v>
<v t="T361"><vh>UIDL</vh></v>
</v>
<v t="T362" a="E"><vh>class POPMaildrop</vh>
<v t="T363"><vh>__init__</vh></v>
<v t="T364"><vh>get_stat</vh></v>
<v t="T365"><vh>get_msg</vh></v>
<v t="T366"><vh>delete_msg</vh></v>
<v t="T367"><vh>update</vh></v>
<v t="T368"><vh>reset</vh></v>
</v>
<v t="T369"><vh>POP3Logger</vh></v>
<v t="T370"><vh>mydebug</vh></v>
<v t="T371"><vh>class Error</vh></v>
<v t="T372"><vh>_digest</vh></v>
<v t="T373"><vh>_get_no</vh></v>
</v>
<v t="T374" a="E"><vh>@file oldstuff/hackdb.py</vh></v>
<v t="T375"><vh>INSTALL-old</vh></v>
<v t="T376"><vh>@file oldstuff/popserve3.py</vh>
<v t="T377"><vh>__doc__</vh></v>
<v t="T378"><vh>imports</vh></v>
<v t="T379"><vh>POP3Logger</vh></v>
<v t="T380"><vh>mydebug</vh></v>
<v t="T381"><vh>class Error</vh></v>
<v t="T382"><vh>class Authenticator</vh></v>
<v t="T383"><vh>class Maildrop</vh>
<v t="T384"><vh>__init__</vh></v>
<v t="T385"><vh>get_stat</vh></v>
<v t="T386"><vh>get_msg</vh></v>
<v t="T387"><vh>delete_msg</vh></v>
<v t="T388"><vh>update</vh></v>
<v t="T389"><vh>reset</vh></v>
</v>
<v t="T390" a="E"><vh>class POPSession</vh>
<v t="T391"><vh>attributes</vh></v>
<v t="T392"><vh>__init__</vh></v>
<v t="T393"><vh>write_ok</vh></v>
<v t="T394"><vh>write_multi</vh></v>
<v t="T395"><vh>write_raw</vh></v>
<v t="T396"><vh>__check_state</vh></v>
<v t="T397"><vh>QUIT</vh></v>
<v t="T398"><vh>USER</vh></v>
<v t="T399"><vh>PASS</vh></v>
<v t="T400"><vh>STAT</vh></v>
<v t="T401"><vh>LIST</vh></v>
<v t="T402"><vh>RETR</vh></v>
<v t="T403"><vh>DELE</vh></v>
<v t="T404"><vh>NOOP</vh></v>
<v t="T405"><vh>RSET</vh></v>
<v t="T406"><vh>TOP</vh></v>
<v t="T407"><vh>UIDL</vh></v>
</v>
<v t="T408"><vh>_digest</vh></v>
<v t="T409"><vh>_get_no</vh></v>
<v t="T410" a="E"><vh>class POPRequestHandler</vh>
<v t="T411"><vh>attributes</vh></v>
<v t="T412"><vh>handle</vh></v>
</v>
<v t="T413"><vh>class POPserver</vh></v>
<v t="T414"><vh>MAINLINE</vh></v>
</v>
<v t="T415"><vh>@file oldstuff/_popserve.py</vh>
<v t="T416"><vh>globals</vh></v>
<v t="T417"><vh>imports</vh></v>
<v t="T418"><vh>undeleted_msgs</vh></v>
<v t="T419" a="E"><vh>class UserRegistry</vh>
<v t="T420"><vh>__init__</vh></v>
<v t="T421"><vh>has_user</vh></v>
<v t="T422"><vh>correct_login</vh></v>
<v t="T423"><vh>get_maildrop</vh></v>
</v>
<v t="T424" a="E"><vh>class Maildrop</vh>
<v t="T425"><vh>__init__</vh></v>
<v t="T426"><vh>get_msg_count</vh></v>
<v t="T427"><vh>get_msg_size_total</vh></v>
<v t="T428"><vh>delete_msg</vh></v>
<v t="T429"><vh>msg_exists</vh></v>
<v t="T430"><vh>send_uidl</vh></v>
<v t="T431"><vh>send_list</vh></v>
<v t="T432"><vh>send_msg</vh></v>
</v>
<v t="T433" a="E"><vh>class SingleFileMaildrop</vh>
<v t="T434"><vh>__init__</vh></v>
<v t="T435"><vh>_read_mailfile</vh></v>
<v t="T436"><vh>get_msg_count</vh></v>
<v t="T437"><vh>get_msg_size_total</vh></v>
<v t="T438"><vh>delete_msg</vh></v>
<v t="T439"><vh>msg_exists</vh></v>
<v t="T440"><vh>send_msg</vh></v>
<v t="T441"><vh>send_uidl</vh></v>
<v t="T442"><vh>send_list</vh></v>
</v>
<v t="T443" a="E"><vh>class SingleUnixFileMaildrop</vh>
<v t="T444"><vh>_read_mailfile</vh></v>
</v>
<v t="T445" a="E"><vh>class SingleFileMailMaildrop</vh>
<v t="T446"><vh>_read_mailfile</vh></v>
</v>
<v t="T447" a="E"><vh>class POPSession</vh>
<v t="T448"><vh>attributes</vh></v>
<v t="T449"><vh>__init__</vh></v>
<v t="T450"><vh>wait</vh></v>
<v t="T451"><vh>user</vh></v>
<v t="T452"><vh>password</vh></v>
<v t="T453"><vh>stat</vh></v>
<v t="T454"><vh>dele</vh></v>
<v t="T455"><vh>retr</vh></v>
<v t="T456"><vh>noop</vh></v>
<v t="T457"><vh>uidl</vh></v>
<v t="T458"><vh>list</vh></v>
<v t="T459"><vh>quit</vh></v>
<v t="T460"><vh>get_greeting</vh></v>
<v t="T461"><vh>get_maildrop</vh></v>
<v t="T462"><vh>error</vh></v>
<v t="T463"><vh>send_ok</vh></v>
</v>
<v t="T464"><vh>MAINLINE</vh></v>
</v>
</v>
</v>
<v t="T465"><vh>PyProxy</vh>
<v t="T466" a="E" tnodeList="466,467,468,469,470,471,472,474,475,477,478,479,480,481,482,484,485,486,487,488,489,490,492,493,494"><vh>@file pyproxy.py</vh>
<v t="T467"><vh>Constants</vh></v>
<v t="T468"><vh>imports</vh></v>
<v t="T469" a="E"><vh>class WebUI</vh>
<v t="T470"><vh>__init__</vh></v>
<v t="T471"><vh>run</vh></v>
<v t="T472"><vh>setCSS</vh></v>
<v t="T473" a="E"><vh>Display Pages</vh>
<v t="T474"><vh>page_main</vh></v>
<v t="T475"><vh>page_template</vh></v>
</v>
<v t="T476" a="E"><vh>Utilities</vh>
<v t="T477"><vh>envDump</vh></v>
</v>
</v>
<v t="T478" a="E"><vh>class HTTPServer</vh>
<v t="T479"><vh>attribs</vh></v>
<v t="T480"><vh>__init__</vh></v>
<v t="T481"><vh>run</vh></v>
</v>
<v t="T482" a="E"><vh>class HTTPRequestHandler</vh>
<v t="T483" a="M"><vh>attributes</vh></v>
<v t="T484"><vh>__init__</vh></v>
<v t="T485"><vh>do_GET</vh></v>
<v t="T486"><vh>do_POST</vh></v>
<v t="T487"><vh>do_HEAD</vh></v>
<v t="T488"><vh>run_cgi</vh></v>
<v t="T489"><vh>translate_path</vh></v>
<v t="T490"><vh>log_message</vh></v>
</v>
<v t="T491" a="E"><vh>Global Functions</vh>
<v t="T492"><vh>runServer</vh></v>
<v t="T493"><vh>log</vh></v>
</v>
<v t="T494"><vh>MAINLINE</vh></v>
</v>
</v>
</vnodes>
<tnodes>
<t tx="T1"></t>
<t tx="T2">@others
</t>
<t tx="T3">@
Notes on freemail protocols

1) All messages are sent to/from 'mailsites'

2) freemail is highly spam-resistant - before you send to someone, you have to
   go through a protocol to get their permission. Think of it as having to
   'knock on someone's door' before they open their door and listen to you.

3) What is a 'mailsite'?

A mailsite is a kind of freesite, with certain information in certain places.

With email, the concept is that of sending to 'user' at 'domain' - via the address
'user@domain'.

With 'freemail', we use the Freesite SSK hash as a metaphor for a 'domain'.

For example, you can send email to:

    fred@gYOSMewAxI8Hy5ZkQ4Ky2WeKUdoBCMA.free

With this address, freemail will target the freesite:

    SSK@gYOSMewAxI8Hy5ZkQ4Ky2WeKUdoBCMA/fred//

4) Can a 'mailsite' live within a 'freesite'?

Sort of. We use a convention here.
If the freesite lives at 'SSK@sitehash/sitename//', we should locate the
mailsite at 'SSK@sitehash/sitename/freemail//'.
That way, the SSK public/private keys for the freesite can be used for
the mailsite as well.

For example, The Freedom Engine lives at:
     SSK@rBjVda8pC-Kq04jUurIAb8IzAGcPAgM/TFE//

If the TFE site owner set up a mailsite, the mailsite URI would be at:
     SSK@rBjVda8pC-Kq04jUurIAb8IzAGcPAgM/TFE/mailsite//

With this convention, freemail can do the DBR insertions independently of those
of the mail freesite.

However, many people will want to run mailsites that aren't connected to any
freesites. In such a case, they'll just generate their own SSK public/private
keys, and their mailsite URI might be:

    SSK@ng7XyA4fmPWbAm5PhIJM1HFFStY/mailsite//

5) What's in a 'mailsite'?

It is highly recommended that mailsites be inserted as DBR-based freesites.

A mailsite, as a DBR freesite, must contain at least the following docs:

    //index.html - for the benefit of those who try to browse the mailsite
                   as if it were a freesite. Contains info about freemail,
                   including links to the freemail freesite

    //session    - this is a document that provides the access details required for
                   other mailsites to initiate relationships with this mailsite.
                   This document consists of lines separated by newlines '\n' only.
                   Freenet metadata mimetype for this document should be 'text/plain'.

               Line 1 - the string 'Freemail'
               Line 2 - a KSK@ URI for receiving inbound CTS/RTS messages
               Line 3 - a number, indicating the next available 'slot' for inserting
                        RTS/CTS messages on this queue
               Lines 4-n - an Ascii-armoured SSLCrypto public key, to be used by
                        peers when encrypting to this mailsite, also used for
                        verifying signatures of RTS/CTS messages sent from this mailsite.


6) What's this 'RTS'/'CTS'?
   How do I establish contact with another mailsite?

'RTS' means 'Request to Send', or, "Hi, I want to start sending messages to you".
'CTS' means 'Clear to Send', or 'Hi, I'm now ready to receive messages from you.

To send messages to a given mailsite for the first time, you must send that
mailsite an 'RTS' message, and get a 'CTS' message back.

When Alice first sends mail to 'bob@bobhash.free', freemail checks to see
if the peer 'bob' is already known. If not, then the following protocol happens:

    1) Alice's mailsite generates a new SSK keypair
       (Alice2BobPUB, Alice2BobPRIV)
    2) Alice's mailsite retrieves SSK@bobhash/bob//key, to get Bob's
       public encryption key
    3) Alice's mailsite retrieves SSK@bobhash/bob//session, to get the KSK URI
       (call this KSK@bobrts), and the next available slot (say, 42).
    4) Alice's mailsite composes a message, containing Alice's mailsite
       URI, plus the new SSK public key (Alice2BobPUB). The message effectively
       says, "Hi Bob, I'm Alice, I want to talk to you, and my messages to you
       can be retrieved from SSK@Alice2BobPUB/0, SSK@Alice2BobPUB/1, ...
       This message is encrypted against Bob's public crypto key
    5) Alice's mailsite tries to insert this message into the key:
       KSK@bobrts-42. If that fails, it tries 'KSK@bobrts-43', and so on, until
       there is no collision (which means a new slot has been found).

    Now, over to Bob

    6) Bob is polling his session queue. He picks up 'KSK@bobrts-44'. He now
       knows that Alice wants to send messages to him.
    7) Bob uses Alice's mailsite URI to get Alice's public crypto key, and the
       'secret' SSK URI on which to listen for messages from Alice. This info
       is encrypted, so that if anyone reads Bob's session queue, they can't
       tell that it's Alice wanting to talk to him
    8) Bob then writes a message, containing Alice's freesite URI, plus the
       'listen' SSK uri which Alice sent, plus a public encryption key for
       messages. This message means, "Ah, hi Alice,
       I got your RTS message - all is fine - from now on, I'll listen for
       messages from you on SSK@Alice2BobPUB/n and you should encrypt them
       using this key I've sent you.
    9) Bob retrieves Alice's 'session' KSK key, and posts the message there.

    Back to Alice

    10) Alice receives Bob's 'CTS' message, and from then knows that she
        can send messages to Bob, and Bob will be listening for these
        messages. She also has the public key against which to encrypt
        these messages.

    Now, for Alice to send Bob a message:

    11) Alice composes message in her email client

    12) Alice sends the message, using an email 'identity' that's configured
        to use the Freemail local SMTP server

    13) Freemail encrypts the message body against the public key which
        Bob sent in his 'CTS' message, and inserts this into Freenet as
        'SSK@Alice2Bob/n'

    Over to Bob

    14) From now on, Bob's mailsite regularly polls SSK@Alice2BobPUB/n...,
        retrieving messages.
    15) When a message comes in, Bob's mailsite decrypts it against the private
        key corresponding to the public key he issued to Alice, and transfers
        it to his freemail message store.
    16) Freemail then sends back a 'confirm' message to Alice, indicating to
        her freemail that he got the message.
    16) Bob's email client fetches the plaintext message via freemail's local
        POP server

Note that this is asymmetrical. The email 'relationship' is one way - Bob is now
able to listen for emails from Alice, and to receive these. But there is no
provision at this time for Alice to receive messages from Bob.

Typically, Bob will send an email reply to Alice. For the first email message,
Bob will RTS to Alice, and Alice will CTS back, after which Alice can receive
messages from Bob.

There is one huge reason for making this protocol asymmetrical, and requiring
that it happen separately in each direction: Due to the lag involved in Freenet,
it's very possible for Alice and Bob to be RTS'ing each other simultaneously
without knowing it. It keeps the protocol simpler yet robust.

---

All this is definitely an onerous task to set up - thankfully, it only needs
to be done once for each new mailing relationship.

Now, if Bob starts getting spammed on his session queue, he can simply insert a
new 'KSK@bobsession' URI into //freemail/session on his freesite.

If Alice starts spamming Bob, then he can simply 'blacklist' her. She can keep
spamming to her heart's content, but Bob is no longer polling 'SSK@Alice2BobPIB/n...'
so he won't waste his time and bandwidth pulling in her messages.

7) Why should a mailsite have such a 'dynamic' session URI and crypto pubkey?

The purpose of a changeable session URI is to partly defend against spam attacks.

The purpose of a changeable public crypto key is in case someone's public key
gets compromised.

-------------------------------------------------
MESSAGE FORMATS
-------------------------------------------------

Format of 'session' messages:

A 'session' message will be either an 'rts' or a 'cts'.

'RTS' messages have the following format:

    Freemail
    rts
    &lt;sender-address&gt;
    &lt;recipient-address&gt;
    &lt;sender-queue-uri&gt;

where:

    &lt;sender-address&gt; is the freemail address of the
    sender, eg 'fred@blahblahblahPAgM'

    &lt;recipient-address&gt; is the freemail address of
    the recipient, eg 'mary@yadayadayadaPAgM'

    &lt;sender-queue-uri&gt; is an SSK uri on which the
    recipient should listen for incoming messages
    eg ('SSK@blahblahblah' - each message will be
    written to 'SSK@blahblahblah/0', 'SSK@blahblahblah/1',...)

'CTS' messages have the following format:

    Freemail
    cts
    &lt;sender-address&gt;
    &lt;recipient-address&gt;
    &lt;sender-queue-uri&gt;
    &lt;recipient-public-key&gt;

where:

    &lt;sender-address&gt; is the freemail address of the
    sender, eg 'fred@blahblahblahPAgM'

    &lt;recipient-address&gt; is the freemail address of
    the recipient, eg 'mary@yadayadayadaPAgM'

    &lt;sender-queue-uri&gt; is an SSK uri on which the
    recipient should listen for incoming messages
    eg ('SSK@blahblahblah' - each message will be
    written to 'SSK@blahblahblah/0', 'SSK@blahblahblah/1',...)

    &lt;recipient-public-key&gt; - an SSLCrypto public
    key, ascii armoured - will take several lines


-------------------------------------------------

Format of Mail Messages - DEPRECATED - OBSOLETE - see next section

Once a mailsite has received a 'CTS' message from a peer, the site will have
an SSK private URI which it can post messages to 'SSK@privkey/n', and the
peer will listen for those messages at 'SSK@pubkeyPAgM/n'.

The data posted to SSK@blahblah/n will be encrypted against the peer's
public key (which the peer sent in the CTS).

The plaintext will have three header lines, followed by the body
of the message. Each header line is terminated by a single newline ('\n')

The header line has the following format:

   Freemail
   &lt;type&gt;
   &lt;msghash&gt;

where:

    &lt;type&gt;     is 'Message' or 'Receipt', according to whether we're sending
               a message or confirming we've received a message
    &lt;msghash&gt;  is a hash of the plaintext of the message body. For the whole
               life of a message, from composing, thru sending, receiving and
               confirming, this hash is used as the unique id of the message

----------------------------------------------------

Format of Mail Messages - NEW

Given a confirmed mail queue, which the sender writes to as SSK@privkey/n and
the recipient reads from as SSK@pubkeyPAgM/n, there is no need for any special
headers or other information.

Messages are uniquely identified by a hash of their plaintext.

So what gets written to these slots is simply the full message body,
encrypted against the receiving peer's key.

If the ciphertext is &lt; 32k in size, the message will be written directly at
this slot.

But SSK keys may not exceed 32k. So if the message ciphertext exceeds that size,
then the ciphertext will be inserted as a CHK, and an empty key with 'redirect'
metadata will be written to the slot.

Note here that the python 'freenet' module handles this automatically in its
node.put() routine.
</t>
<t tx="T4">@
Sequence of operation

- config wizard:
    - pretty much unchanged
    - may need to remove some general fields

- startup:
    - temporarily stick a sleep loop into main fcp thread

- create identity
    - identity fields:
        - name (prompted as '&lt;name&gt; @ freemail)
        - comments
        - optional password

    - dbAddNewIdentity() changed

    - http create identity pages:
        - add/remove fields, validation
        - insermediate save page:
            - inform user we're about to generate crypto key and insert
              identity record into freenet, that it could take a while
            - display 'success' record only when insertion complete

    - dbAddIdentity changes

- http display identities page:
    - display new URI

- http display identity page:
    - display stats
    - display password, if present

- send a message:
    - retrieve peer's site, get the KSK queue uri, get peer's key
    - encrypt/sign message, insert into peer's queue

- receive a message:
    - if can't decrypt, exit
    - retrieve peer's KSK id record
    - if we can't get peer's id record, or signature doesn't match
        - drop the message quietly, exit
    - if first time from this peer:
        - if password:
            - if message contains password:
                - set ok-to-send, save peer record
            - else:
                - bounce back to peer with inserted text
        - else just set ok-to-send, save peer record
    - elif subsequent:
        - retrieve/compare peer's id record
        - if this differs from recorded peer's id record:
            - send bounce to peer, exit
    - if it's a receipt:
        - look up message and mark as delivered
    - else:
        - send off a receipt

- scheduler:
    - one thread per active polling slot - much faster
    - one thread per item on outbound queue
    - manager thread simply looks for outbound items, plus inactive slots due for polling
      and dispatches threads

- db changes
    - no need for global slots tables - just hang 'cells' of slots off each identity record
    - new field in slot table - 'slotIsActive', indicating whether the slot is currently being polled
    - ditto for outbound messages
    - simplify 'mailing relationships':
        - no special SSK queue
        - no dedicated crypto key
        - no rts/cts
        - just store initial peer record

- general:
    - abolish thread limit - if user creates too many identities, tough shit

- new key formats:

    - KSK@freemail-myidname - holds the identity record, in plaintext - this is a dbr file:
        - FreemailIdentity
          Version=&lt;protocolversion&gt;
          Address=KSK@freemail-myidname
          PasswordRequired=&lt;password-hash&gt; (or none)
          NextSlot=&lt;slot-no&gt;
          CryptoKey=&lt;one-line ascii-armoured key&gt;
          End

    - KSK@freemail-myidname-nnn - holds each incoming message/receipt,
      encrypted to recipient, signed by sender
        - FreemailMessage
          Version=&lt;protocolversion&gt;
          Type=&lt;msgtype&gt; - one of 'bounce', 'message', 'receipt', 'resend'
          From=&lt;sender-address&gt;
          To=&lt;recipient-address&gt;
          Seq=&lt;message sequence no&gt; - nnn if sent first time, or seq of message being re-sent or bounced
          Body=&lt;one-line ascii-armoured message&gt;
          End

</t>
<t tx="T5">@
3 types of mailing list:
    1. FreeForAll:
        - the 'in' queue and the 'out' queue are the same - a KSK queue
        - no receipts/confirmations
        - no need to subscribe
        - does not require central server

    2. Private:
        - separate 'in' and 'out' queues
        - 'in' queue is an SSK sequence, where only members have the SSK
          private key
        - 'out' queue is an SSK sequence, where only the owner has the
          SSK private key, and where only members have the decryption key
        - options:
            - restricted members list - yes/no
            - list owner vets all messages, or all messages get automatically
              reposted from in to out

mailing list record contents:
    - amOwner:
        - flag which indicates that we own this list
    - isFreeForAll:
        - flag which indicates if this is a free-for-all list
          if 1, then the list URI for reading and writing is a single queue
          if 0, then list has separate in/out queues, and the out queue is
          an SSK whose private key is held by list owner
    - isPrivate:
        - flag which indicates whether the list is restricted to
          a known set of subscribers.
        - if 1, then messages will only be transferred to the 'out' list if
          the sender is on the subscribers list; also, the 'in' queue is SSK
        - if 0, then all signed/verified messages will automatically relay to
          the 'out' queue, and the 'in' queue will be a KSK
    - isModerated:
        - indicates whether messages must first be approved by the moderator
        - if 1, then messages will only be auto-relayed if the sender is flagged
          such
    - subscribers:
        - only valid if 'amOwner' and 'isPrivate' are 1. This is a cell object, containing the
          official subscribers as keys.
        - Messages received from anyone not on this list will be silently discarded
        - each subscriber record is itself a cell object, with the attributes:
            - privilege:
                - one of 'auto', 'review' and 'mute'
                - 'auto' means auto-deliver
                - 'review' means that the moderator must vet
                - 'mute' is reserved for miscreant subscribers, who are allowed
                  to read messages but cannot post them
    - subscribersLocal:
        - list of local identities which are subscribed to the list
        - when messages are retrieved from the list, they will be posted to each local
          subscriber's inbox
    - listAddr:
        - address of list - listname@lists.blahblah.freemail
    - listName:
        - name of list
    - listSiteUri:
        - public uri of the listsite
    - listSiteUriPriv:
        - private uri of the listsite, for inserting
    - listSiteInsertPeriod:
        - interval in seconds for inserting listsite, usually 86400 (1 day)
    - listSiteLastInserted:
        - absolute date in seconds when listsite was last inserted
    - listSiteRefreshInProgress:
        - indicates whether a refresh of the listsite is presently occurring
    - listInUri:
        - sequence uri for list owner to retrieve messages
        - if isFreeForAll, this will be a KSK. Otherwise, an SSK
    - listInUriPriv:
        - sequence uri for posting messages
        - if NOT isFreeForAll, this is a private SSK key distributed to subscribers
    - listInUriNext:
        - current polling slot number - bottom of window
    - listInSlots:
        - slotmap for owner to poll for messages from subscribers
    - listInSskPub:
        - public SSK key for 'in' list
    - listInSskPriv:
        - private SSK key for 'in' list
    - listOutUri:
        - sequence URI for subscribers to retrieve messages
    - listOutUriPriv:
        - sequence private URI for owner to insert messages
    - listOutUriNext:
        - next slot for owner to post messages
    - listOutSlots:
        - slotmap for subscriber to poll for messages from others
    - listOutSskPub:
        - public SSK key for 'out' list
    - listOutSskPriv:
        - private SSK key for writing to 'out' list
        - only possessed by list owner
    - listInCryptoKey:
        - encryption key of the list owner, used for encrypting
          inbound messages to list owner
    - listOutCryptoKey:
        - encryption key issued to subscribers, enabling them
          to decrypt messages posted to the 'out' queue

mailing list 'listsite' contents:

    FreemailList
    Version=&lt;protocolVersion&gt;
    Address=&lt;listAddr&gt;
    IsFreeForAll=1|0
    IsPrivate=1|0
    IsModerated=1|0
    InUri=&lt;listInUri&gt;
    OutUri=&lt;listOutUri&gt;
    NextSlot=&lt;listInUriNext&gt;
    CryptoKey=&lt;listInCryptoKey&gt;
    End

Message types:
    1. Ordinary inbound message post - as for normal mail message
    2. Ordinary oubound message post - as for normal mail message
    3. Welcome to Private Group:
        FreemailListMessage
        Version=&lt;protocolVersion&gt;
        Type=welcome
        From=&lt;listAddr&gt;
        To=&lt;subscriber address&gt;
        InUriPriv=&lt;listInUriPriv&gt;
        CryptoKeyOut=&lt;listOutCryptoKey&gt;
        End

    4. Gag Notice:
        - sent to miscreants who abuse the list
    5. Ungag notice:
        - forgiveness of miscreants
    6. Group Termination Notice:
        - group is shutting down, perhaps relocating to a new group
          to evict one or more miscreants
</t>
<t tx="T6"></t>
<t tx="T7"></t>
<t tx="T8"></t>
<t tx="T9"></t>
<t tx="T10"></t>
<t tx="T11"></t>
<t tx="T12"></t>
<t tx="T13"></t>
<t tx="T14"></t>
<t tx="T15"></t>
<t tx="T16"></t>
<t tx="T17"></t>
<t tx="T18"></t>
<t tx="T19"></t>
<t tx="T20"></t>
<t tx="T21"></t>
<t tx="T22"></t>
<t tx="T23"></t>
<t tx="T24"></t>
<t tx="T25"></t>
<t tx="T26"></t>
<t tx="T27">@first #!/usr/bin/env python
@language python

# hi there - if you're reading this in a conventional code editor,
# you can save yourself a lot of pain and angst if you download
# and install the 'Leo' metastructural editor.

# http://leo.sourceforge.net

# once you have Leo installed, simply open the 'code.leo' file
# and you'll see this file appearing as a tidy tree of logical
# code groupings.

# After using Leo, you may never want to use vi or emacs for
# significant editing again.

"""
FreeMail - the Freenet Mail Transfer Agent software.

Written from Sep 2003 by David McNab &lt;david@freenet.org.nz&gt;
Copyright (c) 2003 by David McNab

Released under the GNU General Public License, the text
of which can be found at http://www.gnu.org

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
"""
@others
</t>
<t tx="T28">try:
    import warnings
    warnings.filterwarnings('ignore',
                            'Python C API version mismatch',
                            RuntimeWarning,
                            )
    warnings.filterwarnings('ignore',
                            'FCNTL',
                            DeprecationWarning,
                            )
                            
except:
    pass

</t>
<t tx="T29">import sys

# vet the python version
pyversOk = 1
if hasattr(sys, 'version_info'):
    # sys.version_info is new in python2.0
    major, minor, micro, rlevel, serial = sys.version_info
    if major == 2:
        if minor &lt; 2:
            pyversOk = 0
        elif minor == 2 and micro &lt; 1:
            pyversOk = 0
if not pyversOk:
    print "You are trying to run FreeMail with Python version:"
    print sys.version
    print "FreeMail requires Python version 2.2.1 or later"
    sys.exit(1)

import os, time, base64, sha, re, string, random, fnmatch
import thread, threading
import socket, SocketServer
import md5, traceback, signal
import BaseHTTPServer
import mimetypes
import getopt
import posixpath
import BaseHTTPServer
import select
import urllib
import cgi
import shutil
import mimetypes
import codecs
import encodings
from StringIO import StringIO
import Queue
from code import InteractiveConsole
try:
    import readline
except:
    pass
import pydoc

#try:
#    import cPickle as pickle
#except:
#    import pickle
import pickle

from pdb import set_trace

# import some other modules that should be with this package
from pyweb import *
import freenet

try:
    import SSLCrypto
    canImportSSLCrypto = 1
except:
    print
    print "***********************************************"
    print "Cannot import the SSLCrypto python extension"
    print
    print "Did you type 'make'?"
    print "Did you follow the instructions in INSTALL?"
    print "***********************************************"
    print
    canImportSSLCrypto = 0
    time.sleep(5)

if canImportSSLCrypto:
    try:
        SSLCrypto.key(256, "ElGamal").exportKeyPrivate()
    except:
        print "SSLCrypto module failing, please contact author"
        print "david@freenet.org.nz"
        print
        print "Traceback follows - send this to author:"
        traceback.print_exc(file=sys.stdout)
        sys.exit(1)

# epydoc seems to need this
#import freemail





</t>
<t tx="T30">days = 86400 # seconds

# build/protocol indicators

version = "0.1-alpha"
build = "020"
protocolVersion = "1"

newTid = 1

oldStdout = sys.stdout

# these hard-coded constants are mainly for testing

socketTimeout = 1800
reportLocks = 0
mailsiteInsertRetryTime = 300

fcpCycleMinTime = 10
fcpAntiThrashTime = 10
fcpThreadLaunchDelay = 1

fcpRunInThreads = 1
fcpRunServerInThread = 1

receiptSendDelay = 0

# slot state constants

SLOT_STATE_EMPTY = 0
SLOT_STATE_USED = 1
SLOT_STATE_BUSY = 2

# message send state constants

TX_STATUS_NOT_SENT = 0
TX_STATUS_AWAITING_RECEIPT = 1
TX_STATUS_CONFIRMED = 2
TX_STATUS_DEAD = 3


# lock object for writing to "freemail.log.crash"
exceptionLogLock = threading.Lock()

interactivePrompt = "FreeMail&gt; "
</t>
<t tx="T31">class FreeMailCannotSend(Exception):
    "Cannot insert outbound message onto peer queue"

class FreeMailNoPeerInfo(Exception):
    "Cannot retrieve peer info"
</t>
<t tx="T32">class freemailServer:
    """
    This is the uber-class that runs a freemail server.
    
    It launches the threads for monitoring RTS/CTS queues,
    monitoring inbound message queues, posting outbound
    messages etc.
    
    Note that some people, for some reason, might want to
    run multiple freemail sites on the one machine.
    """
    @others
</t>
<t tx="T33">def __init__(self, **kw):
    """
    Constructs a mailsite object.
    
    Arguments:
     - conf - either a config file, or an instance of a config object.
       defaults to 'freemail.conf'
    
    Keywords:
     - quiet - don't print log msgs to stdout (in addition to logfile)
     - database - database file
     - verbosity - verbosity level for logging, default 2, 0 (quiet) thru 4 (debug)
     - logFile - logging file, default 'freemail.loq'
     - fcpHost - hostname of computer with Freenet node, default localhost
     - fcpPort - port number of FCP port on machine with Freenet node
     - popPort - port number of FreeMail POP3 interface
     - smtpPort - port number of FreeMail SMTP interface
     - httpPort - port number of FreeMail Web (http) interface
     - telnetPort - port number of FreeMail Telnet interface
     - retryInit - initial 
    """

    # locate db file
    self.filename = kw.get('database', 'freemail.dat')

    # Create the database lock
    self._dbLockObj = threading.RLock()
    self._dbLockLock = threading.Lock()
    self._dbLockStack = []

    # set verbosity
    self._verbosity = kw.get('verbosity', 1)
    self._freenetverbosity = kw.get('freenetverbosity', 1)
    freenet.LOGMSG = self.logFreenet
    freenet.verbosity(self._verbosity - 1)

    # set up logging
    logfile = kw.get('logfile', "freemail.log")
    #print logfile
    self.logfile = logfile
    self.loglock = threading.Lock()

    # set up 'quiet' flag
    self.isQuiet = kw.get('quiet', 0)

    # create peer info requests tracker
    self.activePeerRequests = {}

    # create or open database
    if not os.path.isfile(self.filename):
        self.log(4, "Creating initial database '%s'" % self.filename)
        self.dbInit(**kw)

    self.log(4, "Loading database '%s'" % self.filename)
    try:
        self.dbLoad()
    except:
        self.log(1, exceptionString())
        self.log(1, "*****************\nAAARRRGGGHHH!!!!!\nFreeMail database file '%s' is sorrupted - bailing out" \
                 % self.filename)
        sys.exit(1)

    # set socket timeouts
    #socket.setdefaulttimeout(socketTimeout)

    self.running = 0
    self.mailServersRunning = 0

</t>
<t tx="T34"></t>
<t tx="T35">def runServer(self):
    """
    Runs the server in blocking mode.

    In contrast to 'startServer()', this method does not return immediately.
    It blocks until (or unless) Freemail terminates
    """
    self.startServer()
    try:
        while 1:
            time.sleep(3600)
    except KeyboardInterrupt:
        print "FreeMail server terminated by user"
        if os.path.isfile("freemail.pid"):
            os.unlink("freemail.pid")
</t>
<t tx="T36">def startServer(self):
    """
    Launches the mail server
    
    Dispatches all the various threads

    If invoked with block=0 (default), returns immediately to the caller,
    so the caller can interact in real-time with the internals of the
    freemailServer object, invoke methods, examine data etc.
    """

    self.log(4, "startServer: start")

    # we always run the web interface
    if 1:
        self.startWebServer()
    else:
        print "STARTING WEBSERVER IN MAINLINE"
        self.threadHttpServer()

    # launch applicable threads here
    
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    state = self.db.config.configState
    httpPort = self.db.config.httpPort

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    if state == 'ready':
        # fully configured - start the rest
        self.startMailServers()
        self.startTelnetServer()

        # and fire up the main kernel
        if fcpRunServerInThread:
            self.startFcpEngine()
        else:
            self.threadFcpEngine()

    elif state not in ['new', 'security', 'ports']:
        # still configuring - only start the mail servers
        self.startMailServers()

    time.sleep(2)
    if state != 'ready':
        print "****************************************************"
        print "Freemail node not (fully) configured"
        print "Please point your browser to http://localhost:%s" % httpPort
        print "to complete the web-based configuration process now"
        print "****************************************************"
        print
        return

    print "** Freemail Server version %s build %s now running..." % (version, build)



</t>
<t tx="T37">def startMailServers(self):
    """
    Launches the freemail POP/SMTP servers
    """
    if not self.mailServersRunning:
        thread.start_new_thread(self.threadPopServer, ())
        thread.start_new_thread(self.threadSmtpServer, ())
        self.mailServersRunning = 1
</t>
<t tx="T38">def startWebServer(self):
    """
    Launches the HTTP interface server
    """
    thread.start_new_thread(self.threadHttpServer, ())
    #self.threadHttpServer()
</t>
<t tx="T39">def startTelnetServer(self):
    """
    Launches the Telnet interface server
    """
    thread.start_new_thread(self.threadTelnetServer, ())
</t>
<t tx="T40">def startFcpEngine(self):
    """
    Launches the FCP transport thread(s)
    """
    thread.start_new_thread(self.threadFcpEngine, ())
</t>
<t tx="T41">def stopServer(self):
    """
    Terminates the mailsite, saves the config
    """
    
    # bring down the threads
    
    # save database
    self.dbSave()
</t>
<t tx="T42"></t>
<t tx="T43"></t>
<t tx="T44">def threadPopServer(self):

    prefix = "popmail"

    self.popPrefix = prefix

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    db = self.db
    config = db.config
    popPort = config.popPort

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    try:
        # create dir prefix if nonexistent
        if not os.path.isdir(prefix):
            os.mkdir(prefix)
        for user in db.identities.keys():
            fulldir = os.path.join(prefix, user)
            if not os.path.isdir(fulldir):
                os.mkdir(fulldir)

        # Create a server object
        serv = POPserver(bindaddr='',
                         port=popPort,
                         freemail=self,
                         handlerClass=POPRequestHandler,
                         )

    except:
        self.log(1, "Exception:\n%s" % exceptionString())

    # and launch it
    try:
        serv.run()
    except:
        self.log(1, "Exception:\n%s" % exceptionString())
</t>
<t tx="T45">def check_user(self, attemptuser):
    """
    Returns 1 if user 'attemptuser' exists, 0 if not
    """
    if len(attemptuser.split("@")) == 1:
        found = ''
        shortid = attemptuser.split("@")[0]
        for id in self.db.identities.keys():
            if id.split("@")[0] == shortid:
                if found:
                    self.log(3, "Tried to log in with non-unique short-hand id '%s'" % shortid)
                    return ''
                else:
                    self.log(5, "Shorthand userid '%s' =&gt; '%s'" % (shortid, id))
                    found = id
        return found

    if self.db.identities.has_key(attemptuser):
        return attemptuser
    else:
        return ''

</t>
<t tx="T46">def auth(self, attemptuser, passwd):
    """
    Checks if username and password are valid
    """
    attemptuser = self.check_user(attemptuser)
    if not attemptuser:
        self.log(3, "Can't log in as '%s'" % attemptuser)
        return ""
    
    if not self.db.identities.has_key(attemptuser):
        self.log(3, "No identity '%s'" % attemptuser)
        return ""
    userRec = self.db.identities[attemptuser]

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    userPass = userRec.idPopPassword

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    if (not userPass) or (hash(passwd) == userPass):
        # no password on database, no password given
        self.log(4, "User '%s' logged in" % attemptuser)
        return attemptuser
    else:
        self.log(3, "User '%s', bad password '%s'" % (attemptuser, passwd))
        return ""
</t>
<t tx="T47"></t>
<t tx="T48">def threadSmtpServer(self):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    db = self.db
    config = db.config
    port = config.smtpPort
    allowedHosts = db.smtpHosts

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    try:
        # Create a server object
        serv = SMTPServer(port=port,
                          allowedHosts=allowedHosts,
                          freemail=self,
                          )
        
        # and launch it
        serv.run()

    except:
        self.log(1, "Exception:\n%s" % exceptionString())

</t>
<t tx="T49"></t>
<t tx="T50">def threadHttpServer(self):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    db = self.db
    config = db.config
    httpPort = config.httpPort
    allowedHosts = db.httpHosts

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    self.WebUI = WebUI

    try:
        # Create a server object
        serv = HTTPServer(name="Freemail Web Interface",
                          port=httpPort,
                          log=self.log,
                          owner=self,
                          handlerClass=HTTPRequestHandler,
                          allowedHosts=allowedHosts,
                          webUI=WebUI,
                          )

        # and launch it
        serv.run()

    except:
        self.log(1, "Exception running HTTP Server\n" + exceptionString())
</t>
<t tx="T51"></t>
<t tx="T52">def threadTelnetServer(self):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    db = self.db
    config = db.config
    telnetPort = config.telnetPort

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    try:
        # Create a server object
        serv = TelnetServer(bindaddr='',
                         port=telnetPort,
                         freemail=self,
                         handlerClass=TelnetRequestHandler,
                         )

    except:
        self.log(1, "Exception:\n%s" % exceptionString())

    # and launch it
    try:
        serv.run()
    except:
        self.log(1, "Exception:\n%s" % exceptionString())
</t>
<t tx="T53"></t>
<t tx="T54"></t>
<t tx="T55">def threadFcpEngine(self):
    """
    This thread does the low-level work of the actual Freemail
    mail transport.

    Possible situations/events requiring action (in no particular order)::

     - an identity's mailsite needs to be refreshed
        - dispatch thread to refresh it

     - outbound mail in Unsent queue
        - dispatch thread to:
           - transmit to peer
           - move to 'noreceipt' list

     - outbound message in 'noreceipt' list is due for re-send
        - dispatch thread to re-send it

     - outbound message in 'noreceipt' list has maxed out retries
        - move to 'dead' list
        - send bounce to user

     - found inactive slots within id rx windows
        - dispatch threads to poll these slots

    """
    self.running = 1
    print "** Freemail: FCP Engine now running..."

    # get/create some handy things
    db = self.db
    config = db.config
    log = self.log

    txMaxRetries = config.txMaxRetries
    txBackoffInit = config.txBackoffInit
    txBackoffMult = config.txBackoffMult

    # main loop - keep looking for stuff to do, and dispatching tasks
    self._loopcount = 0
    nodefailcount = 0
    while 1:

        # ticker message
        if self._loopcount % 1 == 0:
            #log(5, "-------------------------------------------------------")
            log(5, "FCP Engine Thread, top of loop, looking for stuff to do")
        self._loopcount += 1

        # make sure node is alive
        nodehandshakefailed = 0
        try:
            node = self.Node()
            try:
                log(5, "trying node handshake")
                node._handshake()
                log(5, "node handshake successful")
            except:
                if nodefailcount % 90 == 0:
                    loglevel = 2
                else:
                    loglevel = 5
                log(loglevel, "Can't talk to freenet node at %s:%s\nRetrying in 10 seconds\n%s" % (
                          config.fcpHost, config.fcpPort, exceptionString()))
                nodefailcount += 1
                nodehandshakefailed = 1

            if nodehandshakefailed == 0 and nodefailcount &gt; 0:
                log(2, "Freenet node at %s:%s is back up - now we can get on with it" % (
                            config.fcpHost, config.fcpPort))
                nodefailcount = 0
        except:
            print "***********************"
            print exceptionString()
            print "***********************"

        if nodehandshakefailed:
            log(3, "waiting to retry node handshake")
            time.sleep(10)
            continue
                          
        #set_trace()
        then = time.time()

        try:
            self.chugFcpEngine()
        except:
            log(2, "Exception in engine main loop:\n%s" % exceptionString())

        now = time.time()
        if now - then &lt; fcpCycleMinTime:
            time.sleep(fcpCycleMinTime - (now - then))
</t>
<t tx="T56">def chugFcpEngine(self):
    """
    This is the guts of the freemail engine, that gets run inside
    a try/except block by thread FcpEngine.
    
    Performs the checks of things needing to be done, and launches
    threads to perform those tasks.
    """
    db = self.db
    config = db.config
    log = self.log

    if fcpRunInThreads:
        # -v-v-v-v-v-v- LOCK DATABASE ----------------
        self._dbLock()

    # -----------------------------------------------------
    # refresh identity mailsites that need it
    idRecs = db.identities.values()
    for idRec in idRecs:
        if (not idRec.idRefreshInProgress):
            #if (time.time() - idRec.idLastInserted) &gt;= idRec.idInsertPeriod:
            if (time.time() - idRec.idLastInserted) &gt;= 3600:
                log(3, "spawning refresh for identity %s" % idRec.idAddr)
                idRec.idRefreshInProgress = 1
                if fcpRunInThreads:
                    thread.start_new_thread(self.fcpRefreshMailsite, (idRec.idAddr,))
                else:
                    self.fcpRefreshMailsite(idRec.idAddr)


    # -----------------------------------------------------
    # launch threads to poll all the empty (and inactive) rx mail slots

    idRecs = db.identities.values()
    for idRec in idRecs:
        # ignore any identities which are currently refreshing
        if idRec.idRefreshInProgress:
            continue

        # get slots list
        last = idRec.idUriNext + config.slotLookAhead + 1
        emptySlots = idRec.idSlots.between(idRec.idUriNext, last)
        log(5, "id %s has empty slots %s" % (idRec.idAddr, emptySlots))
        for slot in emptySlots:
            idRec.idSlots[slot] = SLOT_STATE_BUSY
            if fcpRunInThreads:
                thread.start_new_thread(self.threadFcpPollSlot, (idRec.idAddr, slot))
            else:
                self.threadFcpPollSlot(idRec.idAddr, slot)

    # -----------------------------------------------------
    # launch threads to dispatch the unsent messages

    for msg in db.txNotSent:
        now = time.time()

        # kludge to squish retryBackoff attribute exceptions
        try:
            retryBackoff = msg.retryBackoff
        except:
            try:
                retryBackoff = config.txBackoffInit
            except:
                retryBackoff = 1800
                config.txBackoffInit = 1800
            msg.retryBackoff = retryBackoff

        if now &gt;= msg.lastTry + retryBackoff:
            if not msg.sendInProgress:
                msg.sendInProgress = 1
                if fcpRunInThreads:
                    thread.start_new_thread(self.threadFcpDeliverMsg, (msg, db.txNotSent))
                else:
                    self.threadFcpDeliverMsg(msg, db.txNotSent)

    # -----------------------------------------------------
    # launch threads to retry non-receipted messages

    for msg in db.txNoReceipt:
        now = time.time()

        # kludge to squish retryBackoff attribute exceptions
        try:
            retryBackoff = msg.retryBackoff
        except:
            try:
                retryBackoff = config.txBackoffInit
            except:
                retryBackoff = 1800
                config.txBackoffInit = 1800
            msg.retryBackoff = retryBackoff

        if now &gt;= msg.lastTry + retryBackoff:
            # have we maxed out retries
            if msg.numTries &gt;= config.txMaxRetries:
                # bounce it
                msgHash = msg.msgHash
                log(3, "Message retry count exceeded: bouncing:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                    msg.idAddr, msg.peerAddr, msgHash))
                db.txDead.append(msg)
                subj = "Delivery Failure Notification - retries exceeded"
                body = "\r\n".join([
                    "This is a message from your FreeMail Postmaster,",
                    "A message you previously tried to send through Freemail",
                    "could not be delivered.",
                    "",
                    "We successfully inserted the message into Freenet, but",
                    "never received a receipt, even after %d attempts" % config.txMaxRetries,
                    "",
                    "This may be occuring because the recipient is not running his/her",
                    "FreeMail software.",
                    "",
                    "Sorry, but this is a permanent failure.",
                    "You may be able to contact the recipient by other means,",
                    "and persuade them to keep their FreeMail software running.",
                    "",
                    "The body of the message you tried to send appears below",
                    "-------------------------------------------------------",
                    "",
                    ""]) + self.readFromOutbox(msgHash)
                self.writeFromPostmaster(msg.idAddr, subj, body)
                db.txNoReceipt.removeWhen(msgHash=msgHash)
                    
            elif not msg.sendInProgress:
                log(2, "Re-sending message (retry count=%s):\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                    msg.numTries, msg.idAddr, msg.peerAddr, msg.msgHash))
                msg.sendInProgress = 1
                if fcpRunInThreads:
                    thread.start_new_thread(self.threadFcpDeliverMsg, (msg, db.txNotSent))
                else:
                    self.threadFcpDeliverMsg(msg, db.txNotSent)

    if fcpRunInThreads:
        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------

</t>
<t tx="T57"></t>
<t tx="T58">def threadFcpRefreshMailsite(self, idAddr, **kw):
    """
    Within a thread, (re-)inserts a given identity's mailsite
    """
    try:
        self.fcpRefreshMailsite(idAddr, **kw)
    except:
        self.log(2, "Exception inserting mailsite for peer %s\n%s" % (
                       idAddr, exceptionString()))
</t>
<t tx="T59">def fcpRefreshMailsite(self, idAddr, **kw):
    """
    Re-inserts the identity mailsite.

    Recall that a 'mailsite' is just like a normal freesite, but with
    the requirement for certain documents to be available at certain
    paths.
    """
    db = self.db
    config = db.config
    fcpHost = config.fcpHost
    fcpPort = config.fcpPort
    if kw.has_key("htl"):
        htlSend = kw['htl']
    else:
        htlSend = config.htlSend

    if not idAddr in db.identities.keys():
        raise Exception("No such identity '%s'" % idAddr)

    # set_trace()

    db = self.db
    
    rId = db.identities[idAddr]
    key = SSLCrypto.key(rId.idCryptoKey)
    idName = rId.idName
    idUriSite = rId.idUriSite
    idUriSitePriv = rId.idUriSitePriv
    idUriQueue = rId.idUriQueue
    idUriNext = rId.idUriNext
    sskPub = rId.idSskPub
    sskPriv = rId.idSskPriv

    offset = 0
    insertPeriod = rId.idInsertPeriod

    if rId.idSenderPassword:
        passwordHash = has(rId.idSenderPassword)
    else:
        passwordHash = ''

    #- SSK@blahblah/freemail/myidname - holds the identity record, in plaintext:

    # construct the mailsite access document
    accessdoc = "\n".join(["FreemailIdentity",
                           "Version=%s" % protocolVersion,
                           "Address=%s" % idAddr,
                           "PasswordRequired=%s" % passwordHash,
                           "QueueUri=%s" % idUriQueue,
                           "NextSlot=%s" % idUriNext,
                           "CryptoKey=%s" % b64enc(key.exportKey()),
                           "End",
                           ])

    # insert the freesite
    node = freenet.node(fcpHost, fcpPort, htlSend)

    self.log(4, "htlSend=%d" % htlSend)

    try:
        # insert the DBR pointer
        future = kw.get('future', 0)

        # Create and Insert DBR main pointer key
        metaDbr = freenet.metadata()
        metaDbr.add('', 'DateRedirect',
                    target=idUriSite,
                    increment=insertPeriod,
                    offset=0)

        #print metaDbr
        self.log(4, "about to insert dbr")
        #vOld = freenet.verbosity(4)
        node.put('', metaDbr, idUriSitePriv, htl=htlSend, allowSplitfiles=0)
        #freenet.verbosity(vOld)
        self.log(4, "dbr inserted")

        for i in [future]:
            # Now insert the manifest du jour as the dbr target - now only
            dbrPrefix = freenet.dbr(i, insertPeriod, offset)
            dujourPubUri = freenet.uri("SSK@%sPAgM/%s-freemail/%s" % (sskPub, dbrPrefix, idName))
            dujourPrivUri = freenet.uri("SSK@%s/%s-freemail/%s" % (
                              sskPriv, dbrPrefix, idName), sskpriv=True)
    
            self.log(4, "inserting id record du jour for %s, %d periods ahead\n" % (idName, i))
            # print accessdoc
    
            # generate mimetype metadata for benefit of those accessing this page via web proxy
            metaFile = freenet.metadata()
            metaFile.add('', mimetype="text/plain")

            try:
                keyNow = node.put(accessdoc,
                                  metaFile,
                                  dujourPrivUri,
                                  htl=htlSend,
                                  allowSplitfiles=0)
                self.log(4,
                         "id record du jour for %s inserted\n"
                         "publicURI = '%s'\n"
                         "privateURI = '%s'\n"
                         "future=%s" % (idName, dujourPubUri, dujourPrivUri, i))
                
                insertedOk = 1
                self.log(4, "Successful refresh of mailSite for identity %s" % idAddr)
            except freenet.FreenetKeyCollision:
                insertedOk = 1
                self.log(4, "KeyCollision refreshing mailSite %s, take as success" % idAddr)
        
    except:
        self.log(2, "Failed to refresh mailsite for %s\n%s" % (idAddr, exceptionString()))
        insertedOk = 0
    #set_trace()

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    if insertedOk:
        rId.idLastInserted = time.time()
    else:
        rId.idLastInserted = time.time() - rId.idInsertPeriod - mailsiteInsertRetryTime # try again in one hour
    rId.idRefreshInProgress = 0

    self.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------


</t>
<t tx="T60"></t>
<t tx="T61">def threadFcpRefreshList(self, listAddr, **kw):
    """
    Within a thread, (re-)inserts a given list's listsite
    """
    try:
        self.fcpRefreshMailsite(listAddr, **kw)
    except:
        self.log(2, "Exception inserting mailsite for peer %s\n%s" % (
                       listAddr, exceptionString()))
</t>
<t tx="T62">def fcpRefreshListsite(self, listAddr, **kw):
    """
    Re-inserts the list's listsite.
    """
    db = self.db
    config = db.config
    fcpHost = config.fcpHost
    fcpPort = config.fcpPort
    if kw.has_key("htl"):
        htlSend = kw['htl']
    else:
        htlSend = config.htlSend

    if not listAddr in db.lists.keys():
        raise Exception("No such list '%s'" % listAddr)

    # set_trace()

    db = self.db
    
    rList = db.lists[listAddr]
    key = SSLCrypto.key(rList.listCryptoKey)
    listName = rList.listName
    listUriSite = rList.listUriSite
    listUriSitePriv = rList.listUriSitePriv
    listUriQueue = rList.listUriQueue
    listUriNext = rList.listUriNext
    sskPub = rList.listSskPub
    sskPriv = rList.listSskPriv

    offset = 0
    insertPeriod = rList.listInsertPeriod

    if rList.listSenderPassword:
        passwordHash = has(rList.listSenderPassword)
    else:
        passwordHash = ''

    #- SSK@blahblah/freemail/mylistname - holds the listentity record, in plaintext:

    # construct the mailsite access document
    accessdoc = "\n".join(["FreemailList",
                           "Version=%s" % protocolVersion,
                           "Address=%s" % listAddr,
                           "PasswordRequired=%s" % passwordHash,
                           "QueueUri=%s" % listUriQueue,
                           "NextSlot=%s" % listUriNext,
                           "CryptoKey=%s" % b64enc(key.exportKey()),
                           "End",
                           ])

    # insert the freesite
    node = freenet.node(fcpHost, fcpPort, htlSend)

    self.log(4, "htlSend=%d" % htlSend)

    try:
        # insert the DBR pointer
        future = kw.get('future', 0)

        # Create and Insert DBR main pointer key
        metaDbr = freenet.metadata()
        metaDbr.add('', 'DateRedirect',
                    target=listUriSite,
                    increment=insertPeriod,
                    offset=0)

        #print metaDbr
        self.log(4, "about to insert dbr")
        #vOld = freenet.verbosity(4)
        node.put('', metaDbr, listUriSitePriv, htl=htlSend, allowSplitfiles=0)
        #freenet.verbosity(vOld)
        self.log(4, "dbr inserted")

        for i in [future]:
            # Now insert the manifest du jour as the dbr target - now only
            dbrPrefix = freenet.dbr(i, insertPeriod, offset)
            dujourPubUri = freenet.uri("SSK@%sPAgM/%s-freemail/%s" % (sskPub, dbrPrefix, listName))
            dujourPrivUri = freenet.uri("SSK@%s/%s-freemail/%s" % (
                              sskPriv, dbrPrefix, listName), sskpriv=True)
    
            self.log(4, "inserting list record du jour for %s, %d periods ahead\n" % (listName, i))
            # print accessdoc
    
            # generate mimetype metadata for benefit of those accessing this page via web proxy
            metaFile = freenet.metadata()
            metaFile.add('', mimetype="text/plain")

            try:
                keyNow = node.put(accessdoc,
                                  metaFile,
                                  dujourPrivUri,
                                  htl=htlSend,
                                  allowSplitfiles=0)
                self.log(4,
                         "list record du jour for %s inserted\n"
                         "publicURI = '%s'\n"
                         "privateURI = '%s'\n"
                         "future=%s" % (listName, dujourPubUri, dujourPubUri, i))
                
                insertedOk = 1
                self.log(4, "Successful refresh of mailSite for list %s" % listAddr)
            except freenet.FreenetKeyCollision:
                insertedOk = 1
                self.log(4, "KeyCollision refreshing mailSite %s, take as success" % listAddr)
        
    except:
        self.log(2, "Failed to refresh listsite for %s\n%s" % (listAddr, exceptionString()))
        insertedOk = 0
    #set_trace()

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    if insertedOk:
        rList.listLastInserted = time.time()
    else:
        rList.listLastInserted = time.time() - rList.listInsertPeriod - mailsiteInsertRetryTime # try again in one hour
    rList.listRefreshInProgress = 0

    self.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------


</t>
<t tx="T63"></t>
<t tx="T64">def fcpGetPeerInfo(self, peerAddr, **kw):
    """
    Given a peer freemail address,
    return a dict of the peer's session queue details,
    or None if we can't get these details

    The peer session info dict contains the keys:
     - queueUri - uri object of the peer seq queue to write to
     - nextslot - int - the next sequence number on this queue to try
     - cryptoKey - an SSLCrypto.key object - the peer's public encryption key
    """
    db = self.db
    config = db.config
    log = self.log

    # where's the peer mailsite?
    peerUri = str(freemailAddrToUri(peerAddr))

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # get pertinent settings from config
    host = config.fcpHost
    port = config.fcpPort
    maxRetries = config.rxMaxRetries
    maxRegress = config.getMaxRegress
    rxHtl = config.htlReceive
    txHtl = config.htlSend

    waitBetweenRetries = kw.get("retryWait", 2)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    # get peer record, or create if nonexistent
    if db.peers.has_key(peerAddr):
        peerRec = db.peers[peerAddr]
    else:
        # create a new peer record
        peerRec = cell(dateMailsiteLastSeen=0,
                       nextSlot=0,
                       )
        db.peers[peerAddr] = peerRec
        self.dbSave()

    log(4, "fcpGetPeerInfo: seeking peer record for %s" % peerAddr)

    # calculate how far back we should look.
    # go back as far as the last version of the peer's mailsite, or 
    # config.maxMailsiteRxRegress, whichever is the later
    earliest = dbrStartTime(86400, -config.maxMailsiteRxRegress)
    if peerRec.dateMailsiteLastSeen &gt; earliest:
        earliest = peerRec.dateMailsiteLastSeen


    if 0:
        # ----------------------------------------------------------
        # Now, for a whole new way of retrieving peer mailsite info.
        # The new technique is to launch several threads, each of which
        # tries to retrieve the version of the peer mailsite n periods back.
        # when all the threads are done, we take the most recent available version.
        #
        # This is now essential, because we've changed the DBR period for
        # mailsites to one day. Refer to fcpRefreshMailsite() for more info
        # on this gratuitous abomination
        
        # 
        # thread func to attempt retrieval of mailsite n periods back
        def thrdGetPeerInfo(dispObj, foundDict, nodeObj, peerUri, past, log=log):
            try:
                sessobj = nodeObj.get(peerUri, past=past)
            except:
                log(3, "Failed to get %s, %s periods past" % (peerUri, past))
            else:
                foundDict[past] = sessobj
                log(4, "Got %s, %s periods past" % (peerUri, past))
                if past == 0:
                    log(4, "Got %s, latest version" % peerUri)
                    dispObj.quit()
    
            log(4, "Thread finished for past=%s" % past)
        
        # create the dispatcher to do n simultaneous requests for mailsite
        dispObj = freenet.Dispatcher(thrdGetPeerInfo, maxthreads=config.maxMailsiteRxThreads)
        foundDict = {} # where results (or lack thereof) get reported by threads
    
        #set_trace()
        
        # load the dispatcher, launch it and wait till it completes
        gotWork = 0
        for i in range(0, config.maxMailsiteRxRegress+1):
            if dbrStartTime(86400, -i) &lt;= earliest:
                break
            dispObj.add(foundDict, freenet.node(host, port, rxHtl), peerUri, i, log)
            gotWork = 1
    
        if gotWork:
            log(4, "starting dispatcher, searching %d insert periods..." % dispObj._jobs.qsize())
            dispObj.start()
            log(4, "waiting for dispatcher to finish...")
            dispObj.wait()
            log(4, "dispatcher complete")
        else:
            if hasattr(peerRec, 'info'):
                log(4, "No need to retrieve - we already have latest peer record")
    
                # the peer record contains the exported key, so we'll have to re-instantiate it
                i = peerRec.info
                return {'CryptoKey': SSLCrypto.key(i['CryptoKey']),
                            'QueueUri': i['QueueUri'],
                            'Version': i['Version'],
                            'Address': i['Address'],
                            'PasswordRequired': i['PasswordRequired'],
                            'NextSlot': i['NextSlot'],
                            }
    
    
                return peerRec.info
            else:
                log(4, "Re-retrieving latest version of peer record")
                foundDict[0] = freenet.node(host, port, rxHtl).get(peerUri)
    
        res = foundDict.keys()
        res.sort()
    
        log(4, "foundDict = %s" % foundDict)
    
        if not res:
            # got nothing
            log(2, "Can't find mailsite for '%s', not even %d periods back" \
                       % (peerAddr, config.maxMailsiteRxRegress))
            return None
        
        # the first element in the keys list will be the most recent
        latest = res[0]
        sessobj = foundDict[latest]
    
        # update peer record in database
        k = freenet.node(host, port, rxHtl).get(peerUri, rxHtl, raw=True)
        try:
            peerRec.period = int(k.metadata.map['']['args']['DateRedirect.Increment'], 16)
        except:
            peerRec.period = 86400 # default
        peerRec.dateMailsiteLastSeen = dbrStartTime(peerRec.period, -latest)
    
        self.dbSave()

    if 1:
        # get a freenet node object
        node = freenet.node(host, port, rxHtl)

        # try to retrieve the peer's session info
        numpast = 0
        key = None
        sessobj = None
        searching = 1
        while numpast &lt;= maxRegress and searching:
            numtries = 0
            while numtries &lt;= maxRetries:
                try:
                    # set_trace()
                    sessobj = node.get(peerUri, past=numpast)
                    searching = 0
                    break
                except freenet.FreenetFcpError:
                    raise
                except freenet.FreenetFcpConnectError:
                    raise
                except:
                    log(3,
                        "failed to get sess info for '%s', waiting to retry\n"
                        "tried to get this under uri %s\n"
                        "numtries=%s/%s, numpast=%s/%s" % (peerAddr, peerUri, numtries, maxRetries, numpast, maxRegress),
                        )
                numtries = numtries + 1
                time.sleep(waitBetweenRetries)
            if sessobj:
                break
            numpast = numpast + 1

    # did we get what we want?
    if not sessobj:
        log(2, "giving up on '%s'" % peerAddr)
        return None

    # maybe yes - now try to carve up the data
    raw = sessobj.data
    lines = raw.strip().split("\n")
    dat = {}
    if lines[0] != 'FreemailIdentity':
        log(1, "bad header line for sess info from '%s': %s: " % (peerAddr, lines[0]))
        return None
    if lines[-1] != 'End':
        log(1, "bad footer line for sess info from '%s': %s: " % (peerAddr, lines[-1]))
        return None

    for line in lines[1:-1]:
        try:
            k, v = line.split("=", 1)
        except:
            log(1, "bad line in sess page for peer %s:\n%s" % (peerAddr, line))
            return None
        dat[k] = v

    # instantiate the crypto key
    try:
        dat['CryptoKey'] = SSLCrypto.key(b64dec(dat['CryptoKey']))
    except:
        log(1, "Bad encryption key from peer %s:\n%s" % (peerAddr, exceptionString()))
        return None

    try:
        peerRec.nextSlot = int(dat['NextSlot'])
    except:
        peerRec.nextSlot = 0

    if not hasattr(peerRec, 'info'):
        peerRec.info = {'CryptoKey': dat['CryptoKey'].exportKey(),
                        'QueueUri': dat['QueueUri'],
                        'Version': dat['Version'],
                        'Address': dat['Address'],
                        'PasswordRequired': dat['PasswordRequired'],
                        'NextSlot': dat['NextSlot'],
                        }
    self.dbSave()

    return dat
</t>
<t tx="T65"></t>
<t tx="T66"></t>
<t tx="T67">def fcpGetListInfo(self, listAddr, **kw):
    """
    Given a list freemail address,
    return a dict of the list's session queue details,
    or None if we can't get these details

    The list session info dict contains the keys:
     - queueUri - uri object of the list seq queue to write to
     - nextslot - int - the next sequence number on this queue to try
     - cryptoKey - an SSLCrypto.key object - the list's public encryption key
    """
    db = self.db
    config = db.config
    log = self.log

    # where's the list mailsite?
    listUri = str(freemailAddrToUri(listAddr))

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # get pertinent settings from config
    host = config.fcpHost
    port = config.fcpPort
    maxRetries = config.rxMaxRetries
    maxRegress = config.getMaxRegress
    rxHtl = config.htlReceive
    txHtl = config.htlSend

    waitBetweenRetries = kw.get("retryWait", 2)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    # ----------------------------------------------------------
    # Now, for a whole new way of retrieving list mailsite info.
    # The new technique is to launch several threads, each of which
    # tries to retrieve the version of the list mailsite n periods back.
    # when all the threads are done, we take the most recent available version.
    #
    # This is now essential, because we've changed the DBR period for
    # mailsites to one day. Refer to fcpRefreshMailsite() for more info
    # on this gratuitous abomination
    
    # 
    # thread func to attempt retrieval of mailsite n periods back
    def thrdGetListInfo(dispObj, foundDict, nodeObj, listUri, past, log=log):
        try:
            sessobj = nodeObj.get(listUri, past=past)
        except:
            log(3, "Failed to get %s, %s periods past" % (listUri, past))
        else:
            foundDict[past] = sessobj
            log(4, "Got %s, %s periods past" % (listUri, past))
        log(4, "Thread finished for past=%s" % past)
    
    # create the dispatcher to do n simultaneous requests for mailsite
    dispObj = freenet.Dispatcher(thrdGetListInfo, maxthreads=config.maxMailsiteRxThreads)
    foundDict = {} # where results (or lack thereof) get reported by threads

    # get list record, or create if nonexistent
    if db.lists.has_key(listAddr):
        listRec = db.lists[listAddr]
    else:
        # create a new list record
        listRec = cell(dateMailsiteLastSeen=0,
                       nextSlot=0,
                       )
        db.lists[listAddr] = listRec
        self.dbSave()

    # calculate how far back we should look.
    # go back as far as the last version of the list's mailsite, or 
    # config.maxMailsiteRxRegress, whichever is the later
    earliest = dbrStartTime(86400, -config.maxMailsiteRxRegress)
    if listRec.dateMailsiteLastSeen &gt; earliest:
        earliest = listRec.dateMailsiteLastSeen

    #set_trace()
    
    # load the dispatcher, launch it and wait till it completes
    gotWork = 0
    for i in range(0, config.maxMailsiteRxRegress+1):
        if dbrStartTime(86400, -i) &lt;= earliest:
            break
        dispObj.add(foundDict, freenet.node(host, port, rxHtl), listUri, i, log)
        gotWork = 1

    if gotWork:
        log(4, "starting dispatcher, searching %d insert periods..." % dispObj._jobs.qsize())
        dispObj.start()
        log(4, "waiting for dispatcher to finish...")
        dispObj.wait()
        log(4, "dispatcher complete")
    else:
        if hasattr(listRec, 'info'):
            log(4, "No need to retrieve - we already have latest list record")

            # the list record contains the exported key, so we'll have to re-instantiate it
            i = listRec.info
            return {'CryptoKey': SSLCrypto.key(i['CryptoKey']),
                        'QueueUri': i['QueueUri'],
                        'Version': i['Version'],
                        'Address': i['Address'],
                        'PasswordRequired': i['PasswordRequired'],
                        'NextSlot': i['NextSlot'],
                        }


            return listRec.info
        else:
            log(4, "Re-retrieving latest version of list record")
            foundDict[0] = freenet.node(host, port, rxHtl).get(listUri)

    res = foundDict.keys()
    res.sort()

    log(4, "foundDict = %s" % foundDict)

    if not res:
        # got nothing
        log(2, "Can't find mailsite for '%s', not even %d periods back" \
                   % (listAddr, config.maxMailsiteRxRegress))
        return None
    
    # the first element in the keys list will be the most recent
    latest = res[0]
    sessobj = foundDict[latest]

    # update list record in database
    k = freenet.node(host, port, rxHtl).get(listUri, rxHtl, raw=True)
    try:
        listRec.period = int(k.metadata.map['']['args']['DateRedirect.Increment'], 16)
    except:
        listRec.period = 86400 # default
    listRec.dateMailsiteLastSeen = dbrStartTime(listRec.period, -latest)

    self.dbSave()

    if 0:
        # get a freenet node object
        node = freenet.node(host, port, rxHtl)

        # try to retrieve the list's session info
        numpast = 0
        key = None
        sessobj = None
        while numpast &lt;= maxRegress:
            numtries = 0
            while numtries &lt;= maxRetries:
                try:
                    # set_trace()
                    sessobj = node.get(listUri, past=numpast)
                    break
                except freenet.FreenetFcpError:
                    raise
                except freenet.FreenetFcpConnectError:
                    raise
                except:
                    log(3,
                        "failed to get sess info for '%s', waiting to retry\n"
                        "tried to get this under uri %s\n"
                        "numtries=%s/%s, numpast=%s/%s" % (listAddr, listUri, numtries, maxRetries, numpast, maxRegress),
                        )
                numtries = numtries + 1
                time.sleep(waitBetweenRetries)
            if sessobj:
                break
            numpast = numpast + 1

    # did we get what we want?
    if not sessobj:
        log(2, "giving up on '%s'" % listAddr)
        return None

    # maybe yes - now try to carve up the data
    raw = sessobj.data
    lines = raw.strip().split("\n")
    dat = {}
    if lines[0] != 'FreemailIdentity':
        log(1, "bad header line for sess info from '%s': %s: " % (listAddr, lines[0]))
        return None
    if lines[-1] != 'End':
        log(1, "bad footer line for sess info from '%s': %s: " % (listAddr, lines[-1]))
        return None

    for line in lines[1:-1]:
        try:
            k, v = line.split("=", 1)
        except:
            log(1, "bad line in sess page for list %s:\n%s" % (listAddr, line))
            return None
        dat[k] = v

    # instantiate the crypto key
    try:
        dat['CryptoKey'] = SSLCrypto.key(b64dec(dat['CryptoKey']))
    except:
        log(1, "Bad encryption key from list %s:\n%s" % (listAddr, exceptionString()))
        return None

    try:
        listRec.nextSlot = int(dat['NextSlot'])
    except:
        listRec.nextSlot = 0

    if not hasattr(listRec, 'info'):
        listRec.info = {'CryptoKey': dat['CryptoKey'].exportKey(),
                        'QueueUri': dat['QueueUri'],
                        'Version': dat['Version'],
                        'Address': dat['Address'],
                        'PasswordRequired': dat['PasswordRequired'],
                        'NextSlot': dat['NextSlot'],
                        }
    self.dbSave()

    return dat





    # &amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;&amp;
    # old shit

    hdr = lines[0]
    kskUri = lines[1]
    nextSlot = lines[2]
    ascKey  = lines[3]

    # basic validation
    if len(lines) != 4 or hdr != "Freemail":
        log(1, "bad sess info from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
        return None

    # try to get posting URI
    try:
        kskUri = freenet.uri(kskUri)
    except:
        log(1, "bad queue uri from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
        return None

    # try to get queue slot
    try:
        nextSlot = int(lines[2])
    except:
        log(1, "bad queue slot from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
        return None

    # try to import encryption key
    try:
        listCryptoKey = SSLCrypto.key(lines[3])
    except:
        log(1, "bad crypto key from '%s': excerpt is: " % (listAddr, repr(raw[:30])))
        return None

    # seems ok
    info = {'queueUri' : kskUri,
            'nextslot' : nextSlot,
            'cryptoKey' : listCryptoKey,
            }
    return info

</t>
<t tx="T68"></t>
<t tx="T69">def threadFcpPollSlot(self, idAddr, slotno):
    """
    Thread which polls an incoming message slot, and if the data is valid,
    actions it.

    Involves sending receipts for messages, sticking received messages on
    inbound queue, and some other shit
    """
    try:
        then = time.time()
        self.chugFcpPollSlot(idAddr, slotno)
        now = time.time()
        if now - then &lt; fcpAntiThrashTime:
            time.sleep(fcpAntiThrashTime - (now - then))
    except:
        self.log(2, "Exception polling slot:\nidAddr=%s\nslotno=%s\n%s" % (
                       idAddr, slotno, exceptionString()))
</t>
<t tx="T70">def chugFcpPollSlot(self, idAddr, slotno):

    db = self.db
    config = db.config
    log = self.log

    log(5, "polling slot %s of id %s" % (slotno, idAddr))

    msg = self.fcpGetMsgFromSlot(idAddr, slotno)

    if not msg:
        log(5, "no (valid) message at slot %s of id %s" % (slotno, idAddr))
        return

    # handle message according to its type
    msgType = msg['Type']
    peerAddr = msg['From']

    log(4, "got message on inbound slot:\nType=%s\nidAddr=%s, slot=%s\npeerAddr=%s" % (
        msgType, idAddr, msg['From'], slotno))

    if msgType in ['message', 'resend']:

        body = msg['Body']
        msgHash = hash(body)

        if msgType == 'message':
            # stick on inbound queue
            self.writeToInbox(idAddr, peerAddr, body)

        elif msgType == 'resend':
            # look it up - have we received it before??
            pass

        if receiptSendDelay &gt; 0:
            print "***********************"
            print "DELIBERATELY STALLING RECEIPT TO CAUSE A SEND RETRY"
            print "***********************"
            time.sleep(receiptSendDelay)

        # send receipt
        self.enqueueMessage(idAddr, peerAddr, 'receipt', msgHash)
        log(2, "sent receipt for message:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
            idAddr, peerAddr, msgHash))
        pass

    elif msgType == 'receipt':
        # look up message on non-receipted list

        msgHash = msg['Hash']

        oldMsgs = db.txNoReceipt.select(msgHash=msgHash)
        
        # copy message to 'sent' list
        if not oldMsgs:
            log(2, "got receipt for message we can't remember sending:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                idAddr, peerAddr, msgHash))
        else:
            if len(oldMsgs) &gt; 1:
                log(2, "more than one msg on noreceipt queue:\nidAddr\%s\npeerAddr=%s\nhash=%s" % (
                    idAddr, peerAddr, msgHash))
            else:
                oldMsg = oldMsgs[0]
                db.txSent.append(oldMsg)

                # put a receipt on original sender's POP3 queue, if required
                if config.receiptAll:
                    try:
                        subj = "FreeMail Delivery Receipt"
                        body = "\r\n".join([
                            "This is a message from your FreeMail Postmaster,",
                            "",
                            "A message you previously sent via Freemail",
                            "has been successfully delivered to the recipient.",
                            "",
                            "The body of this message appears below",
                            "-------------------------------------------------------",
                            "",
                            ""]) + self.readFromOutbox(msgHash)
                        self.writeFromPostmaster(oldMsg.idAddr, subj, body)
                    except:
                        log(2, "Failed to put receipt into sender's POP3 mailbox:\n%s" % exceptionString())

        # remove message from noreceipt list
        db.txNoReceipt.removeWhen(msgHash=msgHash)

    elif msgType == 'bounce':
        pass
</t>
<t tx="T71">def fcpGetMsgFromSlot(self, idAddr, slotno):
    """
    Polls a given slot of a given identity's queue.

    If a message is present on that slot, marks the id's slot record as 'used',
    retrieves the message, decrypts/verifies it.

    Arguments:
     - idAddr - the identity whose queue we should poll for message
     - slotno - the slot number to poll

    Returns:
     - a dict of message if successful, None if nothing there
    """

    log = self.log
    db = self.db

    # get identity record
    idRec = self.db.identities[idAddr]

    # determine which actual URI to poll
    pollUri = "%s%s" % (idRec.idUriQueue, slotno)

    # try to get the key
    node = self.Node('get')
    try:
        k = node.get(pollUri)
    except:
        k = None

    # did we get anything?
    if not k:
        log(5, "No message at slot %s of %s" % (slotno, idAddr))
        idRec.idSlots[slotno] = SLOT_STATE_EMPTY
        return None

    # mark slot as used, move id's window if needed
    log(4, "Got message at slot %s of %s" % (slotno, idAddr))
    idRec.idSlots[slotno] = SLOT_STATE_USED
    if idRec.idUriNext &lt;= slotno:
        idRec.idUriNext = slotno + 1

    log(5, "About to decrypt message at slot %d, idAddr=%s" % (slotno, idAddr))

    # get raw content
    cipherText = k.data

    # get our key and decrypt
    idKey = SSLCrypto.key(idRec.idCryptoKey)
    try:
        clearText = idKey.decStringFromAscii(cipherText)
    except:
        log(2, "cannot decrypt msg at slot %d, idAddr=%s" % (slotno, idAddr))
        return None

    log(5, "msg decrypted ok at slot %d, idAddr=%s" % (slotno, idAddr))

    #set_trace()

    # extract message fields
    try:
        msg = self.fcpParseMailMsg(clearText)
    except:
        log(2, "Exception parsing incoming mail message on slot %s of %s:\n%s" % (
            slotno, idAddr, exceptionString()))
        return None
    if not msg:
        log(2, "Parse of message slot %s of %s failed" % (slotno, idAddr))
        return None

    log(5, "msg parsed ok at slot %d, idAddr=%s" % (slotno, idAddr))

    # basic validation (not already done in fcpParseMailMsg
    if msg['To'] != idAddr:
        log(2, "Message addressed to wrong recipient:\nidAddr=%s, slotno=%s\nAddressed to: %s" % (
            idAddr, slotno, msg['To']))
        return None

    # get purported peer info
    try:
        peerAddr = msg['From']
        peer = self.fcpGetPeerInfo(peerAddr)
    except:
        peerAddr = msg['From']
        log(2, "Can't get peer info for received message:\nidAddr=%s\npeerAddr=%s\nslot=%s" % (
            idAddr, peerAddr, slotno))
        return None

    # check the signature
    try:
        peerKey = peer['CryptoKey']
        if not peerKey.verifyString(msg['raw'], msg['signature']):
            raise Exception("Invalid signature")
    except:
        self.log(3, "Bad signature in message:\nidAddr=%s, slotno=%s\npeerAddr=%s\n%s" % (
            idAddr, slotno, peerAddr, exceptionString()))
        return None

    # don't need raw and sig any longer
    del msg['raw']
    del msg['signature']

    # message passes
    log(4, "Got valid message:\nidAddr=%s\npeerAddr=%s\nslot=%s" % (
        idAddr, peerAddr, slotno))
    return msg
</t>
<t tx="T72">def fcpParseMailMsg(self, plainText):
    """
    Breaks down an incoming message into a dict of its parts

    Also, performs some basic validation of message format
    """
    log = self.log

    # extract signature from end
    try:
        plain, sig = plainText.split("&lt;StartPycryptoSignature&gt;")
        sig = "&lt;StartPycryptoSignature&gt;" + sig
    except:
        self.log(2, "message appears not to contain signature")
        return None

    lines = plain.strip().split("\n")
    msg = {}
    if lines[0] != 'FreemailMessage':
        log(1, "bad message header line:\nline='%s'" % lines[0])
        return None
    if lines[-1] != 'End':
        log(1, "bad message footer line:\nline='%s'" % lines[-1])
        return None

    for line in lines[1:-1]:
        try:
            k, v = line.split("=", 1)
        except:
            log(1, "bad line in message:\nline='%s'" % line)
            return None
        msg[k] = v

    # step in and decode body if needed
    if msg.has_key("Body"):
        try:
            msg['Body'] = b64dec(msg['Body'])
        except:
            log(1, "malformed base64-encoded message body")
            return None

    # basic validation
    if not msg.has_key('To'):
        log(2, "Message has no 'To' field:\nidAddr=%s, slotno=%s" % (idAddr, slotno))
        return None
    if not msg.has_key('From'):
        log(2, "Message has no 'From' field:\nidAddr=%s, slotno=%s" % (idAddr, slotno))
        return None
    if not msg.has_key('Type'):
        log(2, "Message has no 'Type' field:\nidAddr=%s, slotno=%s" % (idAddr, slotno))
        return None
    msgType = msg['Type']
    if msgType not in ['message', 'bounce', 'resend', 'receipt']:
        log(2, "Invalid 'Type' field in message:\nidAddr=%s, slotno=%s\nType=%s" % (
            idAddr, slotno, msgType))
        return None

    # types 'message' and 'resend' must have bodies
    if msgType in ['message', 'resend']:
        if not msg.has_key("Body"):
            log("Message type '%s' missing required body\nFrom=%s\nTo=%s\nType=%s" % (
                msg['From'], msg['To'], msg['Type']))
            return None

    # stick signature and raw into dict
    msg['raw'] = plain
    msg['signature'] = sig

    # fine
    return msg

</t>
<t tx="T73"></t>
<t tx="T74">def threadFcpDeliverMsg(self, msgRec, queueObj):
    """
    Attempts to deliver a message to peer,
    and transfers it to another queue depending on outcome
    """

    try:
        self.chugFcpDeliverMsg(msgRec, queueObj)
    except:
        self.log(2, "Exception delivering message\nmsgRec=%s\nqueueObj=%s\n%s" % (
                       msgRec, queueObj, exceptionString()))
</t>
<t tx="T75">def chugFcpDeliverMsg(self, msgRec, queueObj):
    """
    Attempts to deliver a message to peer,
    and transfers it to another queue depending on outcome
    """
    log = self.log
    db = self.db
    config = db.config

    msgType = msgRec.msgType
    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash

    log(4, "attempting to transmit message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
        msgType, idAddr, peerAddr, msgHash))

    # kludge to squish retryBackoff attribute exceptions
    try:
        retryBackoff = msgRec.retryBackoff
    except:
        try:
            retryBackoff = config.txBackoffInit
        except:
            retryBackoff = 1800
            config.txBackoffInit = 1800
        msgRec.retryBackoff = retryBackoff

    try:
        k = self.fcpTransmitMessage(msgRec)
    except:
        log(2, "Exception transmitting message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s\n%s" % (
            msgType, idAddr, peerAddr, msgHash, exceptionString()))
        k = None

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # update the backoff if this is/was not the first attempt
    if msgRec.lastTry == 0:
        # apply initial backoff
        msgRec.retryBackoff = config.txBackoffInit
    else:
        # expand the backoff
        msgRec.retryBackoff = retryBackoff * config.txBackoffMult

    # update retry data
    msgRec.lastTry = time.time()
    msgRec.numTries += 1

    # remove from whence it came
    queueObj.removeWhen(msgHash=msgHash)

    if k is not None:
        # message delivered successfully

        # copy to 'noreceipt' pile if not a receipt, and not there already
        log(2, "Successfully delivered outbound message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
            msgType, idAddr, peerAddr, msgHash))
        if msgType != 'receipt':
            if len(db.txNoReceipt.select(msgHash=msgHash)) == 0:
                db.txNoReceipt.append(msgRec)
    else:
        # couldn't send message
        log(2, "Failed to enqueue outbound message:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
            msgType, idAddr, peerAddr, msgHash))

        if msgRec.numTries &gt;= config.txMaxRetries:
            # maxed out retries- move to dead
            if msgType in ['message', 'resend']:
                try:
                    body = self.readFromOutbox(msgHash)
                except:
                    body = "&lt;message body deleted from datastore&gt;"
            else:
                try:   
                    body = self.readFromInbox(msgHash)
                except:
                    body = "&lt;message body deleted from datastore&gt;"

            # send bounce to original sender
            self.pmMaxedRetries(msgRec, body)

            db.txDead.append(msgRec) # mark as unsent, schedule for retry
            log(2, "outbound message has exceeded retry limit:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                msgType, idAddr, peerAddr, msgHash))
        else:
            # reinstate to notsent pile
            db.txNotSent.append(msgRec) # mark as unsent, schedule for retry
            log(2, "scheduling outbound message for retry:\ntype=%s\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
                msgType, idAddr, peerAddr, msgHash))

    msgRec.sendInProgress = 0

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------</t>
<t tx="T76">def fcpTransmitMessage(self, msgRec):
    """
    Performs the actual work of sticking a message onto a remote
    recipient's queue

    Arguments:
     - msgRec - a freemail message cell object.

    Returns:
     - key object if delivery succeeded, None if failed

    Format of messages is::
        FreemailMessage
        Version=&lt;protocolversion&gt;
        Type=&lt;msgtype&gt; - one of 'bounce', 'message', 'receipt', 'resend'
        From=&lt;sender-address&gt;
        To=&lt;recipient-address&gt;
        Seq=&lt;message sequence no&gt; - nnn if sent first time, or seq of message being re-sent or bounced
        Body=&lt;one-line ascii-armoured message&gt;
        End
        &lt;message signature&gt;
    """
    log = self.log
    db = self.db
    node = self.Node('put')

    msgType = msgRec.msgType
    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    
    # kludge to squish retryBackoff attribute exceptions
    try:
        retryBackoff = int(msgRec.retryBackoff)
    except:
        try:
            retryBackoff = int(config.txBackoffInit)
        except:
            retryBackoff = 1800
            config.txBackoffInit = 1800
        msgRec.retryBackoff = retryBackoff

    if msgType in ['message', 'resend']:
        try:
            body = self.readFromOutbox(msgHash)
        except:
            self.pmMsgDeletedFromOutbox(msgRec)
            raise FreeMailCannotSend("Can't get message body from outbound message store")
    else:
        try:   
            body = self.readFromInbox(msgHash)
        except:
            self.pmMsgDeletedFromInbox(msgRec)
            raise FreeMailCannotSend("Can't get message body from inbound message store")

    # fetch peer's mailsite
    try:
        peerInfo = self.fcpGetPeerInfo(peerAddr)
    except:
        peerInfo = None

    if not peerInfo:
        log(2, "Failed to get peer info:\nidAddr=%s\npeerAddr=%s\n" % (idAddr, peerAddr))
        self.pmNoPeerInfo(msgRec, body)
        return None

    # extract peer stuff
    queueUri = peerInfo['QueueUri']
    peerKey = peerInfo['CryptoKey']

    # if we've reached here, then there *will* be a peer record
    try:
        nextSlotPeerInfo = peerInfo['NextSlot']
    except:
        print "peerInfo = %s" % peerInfo
        raise
    nextSlotDbPeers = db.peers[peerAddr].nextSlot
    nextSlot = max(nextSlotPeerInfo, nextSlotDbPeers)

    # build the message
    msgFlds = ["FreemailMessage",
               "Version=%s" % protocolVersion,
               "Type=%s" % msgType,
               "From=%s" % idAddr,
               "To=%s" % peerAddr,
               "Body=%s" % b64enc(body),
               ]
    if msgType in ['receipt', 'bounce']:
        msgFlds.append("Hash=%s" % hash(body))
    msgFlds.append("End")
    plainText = "\n".join(msgFlds) + "\n"

    # sign and encrypt
    idKey = SSLCrypto.key(db.identities[idAddr].idCryptoKey)
    sig = idKey.signString(plainText)
    plainText += sig

    try:
        cipherText = peerKey.encStringToAscii(plainText)
    except:
        log(2, "sign/encrypt failed:\nidAddr=%s\npeerAddr=%s" % (idAddr, peerAddr))
        return None

    # stick onto peer queue
    try:
        # add 'text/plain' mimetype metadata to ease browsing via web proxy
        metaFile = freenet.metadata()
        metaFile.add('', mimetype="text/plain")

        #vOld = freenet.verbosity()
        #freenet.verbosity(4)
        k = node.putseq(queueUri,
                        startnum=nextSlot,
                        numtries=100,
                        keydata=cipherText,
                        keymeta=metaFile,
                        )
        #freenet.verbosity(vOld)
        
        # update nextslot
        db.peers[peerAddr].nextSlot = k.seq

        log(2, "Message delivery successful:\nidAddr=%s\npeerAddr=%s" % (idAddr, peerAddr))
        return k
    
    except freenet.FreenetRouteNotFound:

        log(2, "got RouteNotFound from node:\nidAddr=%s\npeerAddr=%s" % (
                idAddr, peerAddr))

        # send bounce to sender
        if msgType in ['message', 'resend']:
            self.pmMsgRouteNotFound(msgRec, body)
        elif msgType == 'receipt':
            self.pmReceiptRouteNotFound(msgRec, body)
        raise
        
    except:
        print "*************************"
        print exceptionString()
        print "*************************"
        #freenet.verbosity(vOld)
        log(2,
            "Failed to insert message ciphertext to peer queue:\n"
            "idAddr=%s\n"
            "peerAddr=%s\n"
            "queueUri=%s\n"
            "%s" % (
            idAddr, peerAddr, queueUri, exceptionString()))

        if msgType in ['message', 'resend']:
            self.pmMsgFreenetException(msgRec, body)
        elif msgType == 'receipt':
            self.pmReceiptFreenetException(msgRec, body)
        raise

</t>
<t tx="T77"></t>
<t tx="T78">def pmMsgDeletedFromOutbox(self, msgRec, body=''):

    config = self.db.config
    log = self.log

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    log(2,
        "failed to read outbox msg:\n"
        "idAddr=%s\n"
        "peerAddr=%s\n"
        "hash=%s\n"
        "dir=%s\n"
        "%s" % (idAddr, peerAddr, msgHash, storeDir, exceptionString()))

    # bounce back to sender
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "A message you tried to send could not be delivered.",
        "",
        "Reason: can't get message body from outbound store (did you delete it?)",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "The exception stack trace appears below:",
        "",
        exceptionString(),
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        ])

    self.writeFromPostmaster(idAddr,
                             "Delivery Failure: Unknown Exception",
                             bounce)


</t>
<t tx="T79">def pmMsgDeletedFromInbox(self, msgRec, body=''):

    config = self.db.config
    log = self.log
    
    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    log(2,
        "failed to read inbox msg:\n"
        "idAddr=%s\n"
        "peerAddr=%s\n"
        "hash=%s\n"
        "dir=%s\n"
        "%s" % (idAddr, peerAddr, msgHash, storeDir, exceptionString()))

    # bounce back to sender
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "A message you tried to send could not be delivered.",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "Reason: can't get message body from inbound store (did you delete it?)",
        "",
        "The exception stack trace appears below:",
        "",
        exceptionString(),
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        ])

    self.writeFromPostmaster(idAddr,
                             "Delivery Failure: Unknown Exception",
                             bounce)


</t>
<t tx="T80">def pmNoPeerInfo(self, msgRec, body=''):

    config = self.db.config
    log = self.log

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    if 1:
        print "idAddr=%s" % idAddr
        print "peerAddr=%s" % peerAddr
        print "msgHash=%s" % msgHash
        print "retryBackoff=%s" % retryBackoff
    
    log(2, "Exception retrieving peer info:\nidAddr=%s\npeerAddr=%s\n%s" % (
        idAddr, peerAddr, exceptionString()))

    # send bounce to sender
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "A message you tried to send could not (yet) be delivered.",
        "",
        "Reason: Cannot (yet) find the recipient's in-freenet freemail record",
        "Recipient: %s" % peerAddr,
        "",
        "In trying to find the recipient record, Freenet (or entropy)",
        "reported the following exception:",
        exceptionString(),
        "",
        "Please check that your Freenet/Entropy node is in",
        "good health.",
        "",
        "You might also try increasing the 'receive HTL' setting via the",
        "web interface",
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        "",
        "An excerpt of the undeliverable message appears below",
        "-------------------------------------------------",
        "",
        body[:8192],
        ])
    self.writeFromPostmaster(idAddr,
                             "Delivery Failure: Can't find recipient",
                             bounce)


</t>
<t tx="T81">def pmMsgRouteNotFound(self, msgRec, body=''):

    config = self.db.config

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "A message you tried to send could not (yet) be delivered.",
        "",
        "Reason: got RouteNotFound from Freenet/Entropy node",
        "        when trying to insert the message data.",
        "",
        "Please check that your Freenet/Entropy node is in",
        "good health.",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "Also, you may want to try changing your FreeMail configuration",
        "to set a lower HTL value for inserting.",
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        "",
        "An excerpt of the undeliverable message appears below",
        "-------------------------------------------------",
        "",
        body[:8192],
        ])

    self.writeFromPostmaster(idAddr,
                             "Delivery Failure: RouteNotFound",
                             bounce)


</t>
<t tx="T82">def pmReceiptRouteNotFound(self, msgRec, body=''):

    config = self.db.config

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "I tried to send off a receipt for an incoming message,",
        "but Freenet complained with a RouteNotFound error.",
        "As a result, the person who sent you this message won't",
        "get their receipt, and will re-send the message.",
        "",
        "This is a non-fatal situation that may right itself.",
        "But you may want to check that your Freenet/Entropy node is in",
        "good health",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        "",
        "Also, you may want to try changing your FreeMail configuration",
        "to set a lower HTL value for inserting.",
        "",
        ])

    self.writeFromPostmaster(idAddr,
                             "Couldn't send receipt: RouteNotFound",
                             bounce)
</t>
<t tx="T83">def pmMsgFreenetException(self, msgRec, body=''):

    config = self.db.config

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    # bounce back to sender
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "A message you tried to send could not be delivered.",
        "",
        "Reason: got an exception when trying to insert the message data.",
        "",
        "The exception stack trace appears below:",
        "",
        exceptionString(),
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        "",
        "Please check that your Freenet/Entropy node is in",
        "good health, and try sending the message again.",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "An excerpt of the undeliverable message appears below",
        "-------------------------------------------------",
        "",
        body[:8192],
        ])

    self.writeFromPostmaster(idAddr,
                             "Delivery Failure: Unknown Exception",
                             bounce)

</t>
<t tx="T84">def pmReceiptFreenetException(self, msgRec, body=''):

    config = self.db.config

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "I tried to send off a reciept for an incoming message,",
        "but an exception occurred within the Freenet interface code.",
        "As a result, the person who sent you this message won't",
        "get their receipt, and will re-send the message.",
        "",
        "This is a non-fatal situation that may right itself.",
        "But you may want to check that your Freenet/Entropy node is in",
        "good health",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "The message is scheduled for another delivery attempt in",
        dhms(retryBackoff),
        "",
        "The exception stack trace appears below:",
        "",
        exceptionString(),
        "",
        ])

    self.writeFromPostmaster(idAddr,
                             "Couldn't send receipt: Exception",
                             bounce)


</t>
<t tx="T85">def pmMaxedRetries(self, msgRec, body=''):

    config = self.db.config

    idAddr = msgRec.idAddr
    peerAddr = msgRec.peerAddr
    msgHash = msgRec.msgHash
    retryBackoff = msgRec.retryBackoff
    numTries = msgRec.numTries
    
    bounce = "\r\n".join([
        "This is a message from your FreeMail Postmaster.",
        "",
        "A message you tried to send could not be delivered.",
        "",
        "The retry limit for sending this message has",
        "been exhausted, so I'm giving up - this is a permanent error.",
        "",
        "I'm sorry things didn't work out in this case.",
        "",
        "You will have received a number of retry notifications, which",
        "hopefully have informed you as to the cause of the problem.",
        "and helped you to track down and rememdy the issue.",
        "",
        "idAddr=%s" % idAddr,
        "peerAddr=%s" % peerAddr,
        "hash=%s" % msgHash,
        "",
        "This was sending attempt %d of %d" % (numTries+1, config.txMaxRetries+1),
        "",
        "An excerpt of the undeliverable message appears below",
        "-------------------------------------------------",
        "",
        body[:8192],
        "",
        ])

    self.writeFromPostmaster(idAddr,
                             "Message Delivery Failure - retry count exhausted",
                             bounce)


</t>
<t tx="T86"></t>
<t tx="T87">def enqueueMessage(self, idAddr, peerAddr, msgType, bodyOrHash):
    """
    Sticks a message on the queue for later sending
    """
    db = self.db
    config = db.config

    if msgType in ['message', 'resend']:
        body = bodyOrHash
        # strip any compromising headers from the message
        hdrs, body = body.split("\r\n\r\n", 1)
        lines = re.split("[\\r\\n]+", hdrs)
        for line in lines:
            # self.log(3, "enqueueMessage: line: " + line)
            if line.startswith("X-Mailer:"):
                # self.log(4, "trying to suppress X-Mailer header")
                lines.remove(line)
        hdrs = "\r\n".join(lines)
        body = "\r\n\r\n".join([hdrs, body])

    # allow addresses to end with '.freenet'
    if peerAddr.endswith(".freenet"):
        peerAddr = peerAddr[:-8]
    
    # if this is a ping, simply write it back to inbox
    if peerAddr in ['freemail-ping', 'ping'] or peerAddr.startswith("ping@"):
        hdrs, body = body.split("\r\n\r\n", 1)
        body = hdrs + "\r\n\r\n" + "*** MESSAGE SENT TO 'freemail-ping': ***\r\n\r\n" + body
        self.writeToInbox(idAddr, 'freemail-pong', body)
        self.log(4, "got a ping message, ponging it")
        return
    
    # write message into store directory
    if msgType in ['message', 'resend']:

        msgHash = hash(body)

        # stick in store if this is first time
        if msgType == 'message':
            # stick in message store
            filename = os.path.join(db.config.storeDir, "tx", msgHash)
            fd = open(filename, "wb")
            fd.write(body)
            fd.close()
    else:
        msgHash = bodyOrHash
        body = ''

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # and stick the message onto outbound queue
    db.txNotSent.append(idAddr=idAddr,
                       peerAddr=peerAddr,
                       date=time.time(),
                       msgHash=msgHash,
                       msgLen=len(body),
                       msgChk='',
                       numTries=0,
                       txStatus=TX_STATUS_NOT_SENT,
                       lastTry=0,
                       retryBackoff=config.txBackoffInit,
                       msgType=msgType,
                       sendInProgress=0,
                       )

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    self.dbSave()

    self.log(4, "Enqueued message from '%s' to '%s'" % (idAddr, peerAddr))


</t>
<t tx="T88">def writeToInbox(self, idAddr, fromAddr, body):
    """
    Takes a received message and adds it to the local inbox

    Arguments:
     - inAddr - address of recipient
     - fromAddr - address of peer
     - body - body of message
    """
    msgHash = hash(body)

    # write message into store directory    
    filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
    fd = open(filename, "wb")
    fd.write(body)
    fd.close()

    # "idAddr:S",        # name of local identity which received the message
    # "peerAddr:S",      # name of remote peer which sent the message
    # "date:D",          # UTC python date/time (seconds since epoch, float)
    # "msgHash:S",       # hash of plaintext message body - filename in store
    # "msgChk:S",        # CHK URI of encrypted message
 
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # enter message into received messages table
    if len(self.db.rxMessages.select(msgHash=msgHash)) == 0:
        self.db.rxMessages.append(idAddr=idAddr,
                                  peerAddr=fromAddr,
                                  date=time.time(),
                                  msgHash=msgHash,
                                  msgChk='',
                                  msgLen=len(body),
                                  isDeleted=0)
    else:
        self.log(4, "Received a duplicate of an existing message:\nidAddr=%s\npeerAddr=%s\nhash=%s" % (
            idAddr, fromAddr, msgHash))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    self.dbSave()
</t>
<t tx="T89">def readFromOutbox(self, msgHash):
    """
    Reads in the text of a message from the outbound store
    
    Arguments:
     - msgHash - the hash of the outbound message
    """
    # write message into store directory    
    filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
    fd = open(filename, "rb")
    body = fd.read()
    fd.close()
    return body
</t>
<t tx="T90">def readFromInbox(self, msgHash):
    """
    Reads in the text of a message from the inbound store
    
    Arguments:
     - msgHash - the hash of the inbound message
    """
    # write message into store directory    
    filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
    fd = open(filename, "rb")
    body = fd.read()
    fd.close()
    return body
</t>
<t tx="T91">def writeToOutbox(self, body):
    """
    Writes the text of the message to the local outbound file store
    
    Arguments:
     - body - full string of message

    Returns
     - msgHash - the hash of the outbound message
    """
    # hash it
    msgHash = hash(body)

    # write message into store directory    
    filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
    fd = open(filename, "wb")
    fd.write(body)
    fd.close()
    return msgHash
</t>
<t tx="T92">def writeToInboxRaw(self, idAddr, fromAddr, subject, body):
    """
    Composes a raw message to a local identity, and add
    this to the identity's inbox
    
    Headers get added to the message for the benefit of the client
    """

    # build up a set of message headers
    nowstr = time.asctime(time.localtime(time.time()))
    
    bodyHash = hash(body)
    hdrs = []
    hdrs.append("Return-path: &lt;%s&gt;" % fromAddr)
    hdrs.append("Envelope-to: %s" % idAddr)
    hdrs.append("Delivery-date: %s" % nowstr)
    hdrs.append("Received: from freemail (helo=freemail) by localhost with")
    hdrs.append("        local-smtp (freemail v%s) id %s; %s" % (version, bodyHash, nowstr))
    hdrs.append("From: %s" % fromAddr)
    hdrs.append("Date: %s" % nowstr)
    hdrs.append("To: %s" % idAddr)
    hdrs.append("Message-ID: &lt;%s&gt;" % bodyHash)
    hdrs.append("Mime-Version: 1.0")
    hdrs.append("Content-Type: text/plain; charset=us-ascii")
    hdrs.append("Content-Disposition: inline")
    hdrs.append("Subject: %s" % subject)
    hdrs.append("Sender: %s" % fromAddr)
    hdrs.append("")
    hdrs.append("")
    
    fullBody = "\r\n".join(hdrs) + body
    self.writeToInbox(idAddr, fromAddr, fullBody)

</t>
<t tx="T93">def writeFromPostmaster(self, idAddr, subject, body):
    self.writeToInboxRaw(idAddr,
                         "Freemail Postmaster &lt;postmaster@freemail&gt;", 
                         subject,
                         body)
</t>
<t tx="T94"></t>
<t tx="T95">def _dbLock(self):
    """
    acquires the 'lock' on the database
    
    All code should acquire this lock before accessing the database,
    whether reading or writing
    """

    caller = traceback.extract_stack()[-2]
    full = "%s:%s:%s()" % (os.path.split(caller[0])[1], caller[1], caller[2])

    self._dbLockLock.acquire()

    then = time.time()

    gotit = self._dbLockObj.acquire(False)
    if gotit:
        self._dbLockStack.append(full)

    self._dbLockLock.release()

    if gotit:
        if reportLocks:
            self.log(2, "DBLOCK ACQUIRED by %s" % full, 1)
    else:
        try:
            if reportLocks:
                self.log(2, "DBLOCK WANTED by %s, held by %s" % (full, self._dbLockStack[-1]), 1)
        except:
            if reportLocks:
                self.log(2, "DBLOCK WANTED by %s, held by ???" % full, 1)
        self._dbLockObj.acquire(True)
        self._dbLockStack.append(full)

    now = time.time()
    if reportLocks:
        self.log(2, "DBLOCK ACQUIRED by %s after %f seconds" % (full, now - then), 1)
    self._dbLockStartTime = now
</t>
<t tx="T96">def _dbUnlock(self):
    """
    releases the 'lock' on the database
    """
    #now = time.time()

    caller = traceback.extract_stack()[-2]
    full = "%s:%s:%s()" % (os.path.split(caller[0])[1], caller[1], caller[2])

    if reportLocks:
        self.log(2, "DBLOCK released by %s" % full)
    self._dbLockObj.release()
    self._dbLockStack.pop()

    #self.log(2, "released lock after %f seconds" % (now - self._dbLockStartTime), 1)


</t>
<t tx="T97">def dbLoad(self):
    """
    Loads the configuration from file
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    self.log(4, "Creating db storage object")

    # load database from pickle, incorporate into this object
    fd = open(self.filename, "rb")
    self.db = pickle.load(fd)
    fd.close()

    db = self.db
    config = db.config

    self.dbUpdate()

    self.log(4, "Successfully restored pickled db object")

    if not self.dbIntegrityOk():
        newname = "%s-corrupt-%s" % (self.filename, time.strftime("%Y-%m-%d-%H-%M-%S"))
        self.log(1,
                 "*************\n"
                 "\n"
                 "NO! PLEASE! NO! NO!\n"
                 "\n"
                 "I am **TOAST** !!!\n"
                 "\n"
                 "Database file %s is corrupted\n"
                 "\n"
                 "Saved this bad file to %s\n"
                 "\n"
                 "Please send this file to david@freenet.org.nz\n"
                 "The next time you run FreeMail, it will create a whole new database\n"
                 "***************************\n"
                 "\n" % (
                      self.filename, newname))
        os.rename(self.filename, newname)
        sys.exit(1)

    # frig identity records to mark refresh not in progress, all busy slots inactive
    for id in db.identities.values():
        id.idRefreshInProgress = 0
        id.idSlots.resetBusy()

    # frig tx queues to reset 'in progress' flag
    for msg in db.txNotSent:
        msg.sendInProgress = 0
    for msg in db.txNoReceipt:
        msg.sendInProgress = 0

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    self.log(4, "Database successfully loaded (I think)")
</t>
<t tx="T98">def dbUpdate(self):
    """
    Dodgy method which senses changes of FreeMail builds, and
    updates database accordingly
    """
    
    db = self.db
    config = db.config
    log = self.log
    changed = 0

    # freemailBuild field added from build 012    
    if not hasattr(config, 'freemailBuild'):
        config.freemailBuild = '011'

    dbBuild = config.freemailBuild
    
    if dbBuild == '011':
        # took out slotBackoff and txRetryInterval fields, added txBackoff fields
        log(1, "Updating database from build 011 to build %s" % build)
        del config.slotBackoffInit
        del config.slotBackoffMult
        del db.txNoSite
        del config.txRetryInterval

        config.txBackoffInit = 7200
        config.txBackoffMult = 1.6

        for msg in db.txNotSent.values():
            msg.retryBackoff = config.txBackoffInit
        for msg in db.txNoReceipt.values():
            msg.retryBackoff = config.txBackoffInit

        changed = 1
        log(1, "Database update completed successfully")

    if dbBuild &lt; '015':
        # added telnet server
        config.telnetPort = 10021
        db.telnetHosts = ['127.0.0.1']
        changed = 1

    if dbBuild &lt; '016' or not config.has_key('maxMailsiteRxRegress'):
        config.maxMailsiteRxRegress = 14
        config.maxMailsiteRxThreads = 10
        changed = 1

    if dbBuild &lt; '017' or not db.has_key('lists'):
        # build 17 adds support for mailing lists
        db.lists = cell()
        changed = 1
    
    # save if changed
    if changed:
        config.freemailBuild = build
        self.dbSave()

</t>
<t tx="T99">def dbIntegrityOk(self):
    """
    Perform some integrity checks on the database

    Arguments:
        - db - metakit database object

    Returns 1 if database seems ok, 0 if there's something wrong
    """

    minTableSet = ['config', 'smtpHosts', 'popHosts', 'httpHosts',
                   'identities', 'peers',
                   'txNotSent', 'txNoReceipt', 'txSent', 'txDead',
                   'rxMessages',
                   ]

    tables = self.db._dict.keys()

    #set_trace()

    seemsOk = 1    

    # only integrity check for now is checking that minimal set of tables is present
    for tabName in minTableSet:
        if tabName not in tables:
            self.log(1, "database %s missing table %s" % (self.filename, tabName))
            seemsOk = 0

    return seemsOk
</t>
<t tx="T100">def dbInit(self, **kw):
    """
    Initialises the config object, to create a new mailsite
    
    Arguments:
     - none
     
    Keywords:
     - name - human-readable text name for this identity - mandatory
     - sskpriv - SSK private key. If none, a new SSK gets created
     - sskpub - SSK public key. IF none, a new SSK gets created
    """

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # create database, populate with given info
    self.db = cell()
    db = self.db

    if not os.path.isfile(self.filename):
        # create a basic pickle
        fd = open(self.filename, "wb")
        pickle.dump(db, fd, 1)
        fd.close()

    # retrieve password, if any
    try:
        adminPassword = hash(kw['adminPassword'])
    except:
        adminPassword = ''

    # determine store directory
    try:
        storeDir = kw['storeDir']
    except:
        storeDir = os.path.join(os.path.split(self.filename)[0], "freemail.store")

    # create store directory tree if needed
    if not os.path.isdir(storeDir):
        os.mkdir(storeDir)

    storeDirTx = os.path.join(storeDir, "tx")
    storeDirRx = os.path.join(storeDir, "rx")
    if not os.path.isdir(storeDirTx):
        os.mkdir(storeDirTx)
    if not os.path.isdir(storeDirRx):
        os.mkdir(storeDirRx)

    # --------------------------------------------------------
    # site-wide data
    db.config = cell(
        freemailBuild=build,
        configState=kw.get('configState', "new"),            # for initial config wizard
        adminLogin=kw.get('adminLogin', 'freemail'),         # login name for HTTP admin sessions
        adminPassword=adminPassword,                         # hashed password for HTTP admin sessions
        fcpHost=kw.get('fcpHost', "127.0.0.1"),              # hostname of freenet/entropy node
        fcpPort=kw.get('fcpPort', 8481),                     # FCP port of freenet/entropy node
        smtpPort=kw.get('smtpPort', 25),                     # listen port for incoming SMTP connections
        popPort=kw.get('popPort', 110),                      # listen port for incoming POP3 connections
        httpPort=kw.get('httpPort', 8889),                   # listen port for incoming HTTP admin connections
        telnetPort=kw.get('telnetPort', 10023),              # listen port for incoming Telnet admin connections
        adminTimeout=kw.get('adminTimeout', 3600),           # stay logged in for one hour, timeout in seconds for HTTP admin sessions
        htlSend=kw.get('htlSend', 25),                       # HTL for sending (inserting) messages
        htlReceive=kw.get('htlReceive', 25),                 # HTL for receiving (retrieving) messages
        receiptAll=kw.get('receiptAll', 1),                  # place receipts for all sent messages into
                                                             # sender's POP3 mailbox
        storeDir=storeDir,                                   # store directory
        rxMaxRetries=kw.get('rxMaxRetries', 3),              # maximum number of retries when receiving
        txMaxRetries=kw.get('txMaxRetries', 15),             # maximum number of retries when sending
        txBackoffInit=kw.get('txBackoffInit', 600),          # initial tx retry backoff time, in seconds
        txBackoffMult=kw.get('txBackoffMult', 1.5),          # factor to multiply by tx backoff factor
        getMaxRegress=kw.get('getMaxRegress', 14),           # number of periods to regress with mailsite fetches
        cryptoKeySize=kw.get('cryptoKeySize', 2048),         # strength of crypto keys generated for receiving, in bits
        slotLookAhead=kw.get('slotLookAhead', 2),            # number of receive 'slots' to look ahead of 'next slot'

        maxMailsiteRxRegress=kw.get('maxMailsiteRxRegress', 14), # max periods to regress when fetching peer mailsites
        maxMailsiteRxThreads=kw.get('maxMailsiteRxThreads', 10), # max threads to use for regressed fetches

        )

    # --------------------------------------------------------
    # list of hosts from which we accept SMTP connections
    db.smtpHosts = kw.get('smtpHosts', ['127.0.0.1'])
    
    # --------------------------------------------------------
    # list of hosts from which we accept POP3 connections
    db.popHosts = kw.get('popHosts', ['127.0.0.1'])

    # --------------------------------------------------------
    # list of hosts from which we accept HTTP connections
    db.httpHosts = kw.get('httpHosts', ['127.0.0.1'])

    # --------------------------------------------------------
    # list of hosts from which we accept Telnet connections
    db.telnetHosts = kw.get('telnetHosts', ['127.0.0.1'])

    # --------------------------------------------------------
    # all our local identities
    db.identities = cell()

    # --------------------------------------------------------
    # all known external peers
    db.peers = {}
    
    # ----------------------------------------------------------
    # all mailing lists - the ones we own, and the ones we don't
    db.lists = cell()

    # --------------------------------------------------------
    # store of outbound messages - pending, unconfirmed, sent and dead
    db.txNotSent = cell()
    db.txNoReceipt = cell()
    db.txSent = cell()
    db.txDead = cell()

    # --------------------------------------------------------
    # store of messages received
    db.rxMessages = cell()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    self.dbSave()


</t>
<t tx="T101">def dbSave(self):
    """
    Saves the configuration to file
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # save to temporary file - precaution against this prog being terminated
    # in the middle of a save
    fd = open(self.filename+".new", "wb")
    pickle.dump(self.db, fd)
    fd.close()

    # delete old one, rename new one
    os.unlink(self.filename)
    os.rename(self.filename+".new", self.filename)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T102">def dbCleanup(self):
    """
    Cleans up database and instance refs
    
    Called at the end of each processing run.

    Performs various checks on the database, and deletes obsolete records.
    Also, deletes local instance refs to these records (eg, slot tables in
    self.rxSlots[]).
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # do the business

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
</t>
<t tx="T103">def dbAddIdentity(self, name, **kw):
    """
    Creates a whole new identity (alias) and stores in database

    Arguments:
     - name - compulsory - name of this identity - short string [a-zA-Z0-9_]+
    
    Keywords:
     - cryptoKey - if provided, this is an SSLCrypto private key
     - password - password under which to access this identity's mail via POP3.
       if provided, gets stored hashed in database. If not provided, no password required
     - cryptoKeySize - length of encryption key - defaults to configured value
     - sskPub, sskPriv - SSK keypair
     - insertPeriod - DBR period in seconds, default 259200 (3 days)
     - refreshInProgress - 1 to mark the id insert record as 'in progress', default 0

    Returns:
     - idAddr - the mailing address of the identity
    """
    log = self.log

    node = self.Node()

    password = kw.get('password', '')
    if password:
        password = hash(password)

    mailPassword = kw.get('mailPassword', '')
    if mailPassword:
        mailPassword = hash(mailPassword)

    senderPassword = kw.get('senderPassword', '')

    # generate an SSK keypair if needed
    if not kw.has_key('sskPub'):
        pub, priv = node.genkeypair()
    else:
        pub, priv = kw['sskPub'], kw['sskPriv']
    
    # now we can build identity mailing address
    idAddr = "%s@%s.freemail" % (name, pub)

    db = self.db
    if db.identities.has_key(idAddr):
        raise Exception("identity with address '%s' already exists" % idAddr)

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # generate key
    config = self.db.config
    cryptoKeySize = kw.get('cryptoKeySize', config.cryptoKeySize)
    
    if kw.has_key('cryptoKey'):
        if hasattr(kw['cryptoKey'], 'exportKeyPrivate'):
            cryptoKey = kw['cryptoKey']
        else:
            cryptoKey = SSLCrypto.key(kw['cryptoKey'])
    else:
        cryptoKey = SSLCrypto.key(cryptoKeySize)
    cryptoKeyStr = cryptoKey.exportKeyPrivate()

    idUriSite = "SSK@%sPAgM/freemail/%s" % (pub, name)
    idUriSitePriv = "SSK@%s/freemail/%s" % (priv, name)

    idUriQueue = kw.get("queueUri", "KSK@%s" % randstring() + "-")

    idInsertPeriod = kw.get("insertPeriod", 259200)

    # stick into database
    self.db.identities[idAddr] = cell(idAddr=idAddr,
                                      idName=name,
                                      idPopPassword=password,
                                      idMailPassword=mailPassword,
                                      idUriSite=idUriSite,
                                      idUriSitePriv=idUriSitePriv,
                                      idUriQueue=idUriQueue,
                                      idUriNext=0,
                                      idSskPub=pub,
                                      idSskPriv=priv,
                                      idCryptoKey=cryptoKeyStr,
                                      idSlots = slotmap(),
                                      idInsertPeriod=idInsertPeriod,
                                      idLastInserted=0,
                                      idSenderPassword=senderPassword,
                                      idRefreshInProgress=kw.get('refreshInProgress', 0),
                                      )
    self.dbSave()

    # send a courtesy greeting to the user
    body = "\r\n".join([
        "Hi and Welcome to FreeMail!",
        "",
        "This is a message from your FreeMail Postmaster.",
        "",
        "If you are reading this, then your new identiy,",
        idAddr,
        "was created successfully and is now ready for use",
        "",
        "You can now send emails from this account, and give",
        "out this freemail address to others.",
        "",
        "Best Regards,",
        "Your FreeMail postmaster",
        "",
        ])

    self.writeFromPostmaster(idAddr,
                             "Welcome to FreeMail, %s" % idAddr,
                             body)



    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    return idAddr</t>
<t tx="T104">def dbAddList(self, name, **kw):
    """
    Creates a new mailing list record and stores in database

    Arguments:
     - name - compulsory - name of this mailing list - short string [a-zA-Z0-9_]+
    
    Keywords:
     - cryptoKey - if provided, this is an SSLCrypto private key
     - cryptoKeySize - length of encryption key - defaults to configured value
     - sskPub, sskPriv - SSK keypair
     - insertPeriod - DBR period in seconds, default 259200 (3 days)
     - refreshInProgress - 1 to mark the id insert record as 'in progress', default 0

    Returns:
     - idAddr - the mailing address of the identity
    """
    log = self.log

    node = self.Node()

    password = kw.get('password', '')
    if password:
        password = hash(password)

    mailPassword = kw.get('mailPassword', '')
    if mailPassword:
        mailPassword = hash(mailPassword)

    senderPassword = kw.get('senderPassword', '')

    # generate an SSK keypair if needed
    if not kw.has_key('sskPub'):
        pub, priv = node.genkeypair()
    else:
        pub, priv = kw['sskPub'], kw['sskPriv']
    
    # now we can build identity mailing address
    idAddr = "%s@%s.freemail" % (name, pub)

    db = self.db
    if db.identities.has_key(idAddr):
        raise Exception("identity with address '%s' already exists" % idAddr)

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # generate key
    config = self.db.config
    cryptoKeySize = kw.get('cryptoKeySize', config.cryptoKeySize)
    
    if kw.has_key('cryptoKey'):
        if hasattr(kw['cryptoKey'], 'exportKeyPrivate'):
            cryptoKey = kw['cryptoKey']
        else:
            cryptoKey = SSLCrypto.key(kw['cryptoKey'])
    else:
        cryptoKey = SSLCrypto.key(cryptoKeySize)
    cryptoKeyStr = cryptoKey.exportKeyPrivate()

    idUriSite = "SSK@%sPAgM/freemail/%s" % (pub, name)
    idUriSitePriv = "SSK@%s/freemail/%s" % (priv, name)

    idUriQueue = kw.get("queueUri", "KSK@%s" % randstring() + "-")

    idInsertPeriod = kw.get("insertPeriod", 259200)

    # stick into database
    self.db.lists[idAddr] = cell(idAddr=idAddr,
                                 idName=name,
                                 idPopPassword=password,
                                 idMailPassword=mailPassword,
                                 idUriSite=idUriSite,
                                 idUriSitePriv=idUriSitePriv,
                                 idUriQueue=idUriQueue,
                                 idUriNext=0,
                                 idSskPub=pub,
                                 idSskPriv=priv,
                                 idCryptoKey=cryptoKeyStr,
                                 idSlots = slotmap(),
                                 idInsertPeriod=idInsertPeriod,
                                 idLastInserted=0,
                                 idSenderPassword=senderPassword,
                                 idRefreshInProgress=kw.get('refreshInProgress', 0),
                                 )
    self.dbSave()

    # send a courtesy greeting to the user
    body = "\r\n".join([
        "Hi and Welcome to FreeMail!",
        "",
        "This is a message from your FreeMail Postmaster.",
        "",
        "If you are reading this, then your new identiy,",
        idAddr,
        "was created successfully and is now ready for use",
        "",
        "You can now send emails from this account, and give",
        "out this freemail address to others.",
        "",
        "Best Regards,",
        "Your FreeMail postmaster",
        "",
        ])

    self.writeFromPostmaster(idAddr,
                             "Welcome to FreeMail, %s" % idAddr,
                             body)



    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    return idAddr</t>
<t tx="T105">def dbDelIdentity(self, idAddr):
    """
    Purges a mailing identity

    This involves:
     - removing the identity from the identities table, and the local dicts
     - removing all mailing relationships involving that identity
     - removing all messages to/from that identity

    The database will end up in a form as if the identity never existed.
    """
    db = self.db

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # remove from identities table and local dicts
    del db.identities[idAddr]

    # remove all tx and rx messages and message files
    storeDir = db.config.storeDir

    for c in [db.txNotSent, db.txNoReceipt, db.txSent, db.txDead]:
        for r in c:
            if r.idAddr == idAddr:
                msgpath = os.path.join(storeDir, 'tx', r.msgHash)
                try:
                    os.unlink(msgpath)
                except:
                    self.log(3, "dbDelIdentity: failed to delete tx msg file %s" % r.msgHash)
        c.removeWhen(idAddr=idAddr)

    for r in db.rxMessages:
        if r.idAddr == idAddr:
            msgpath = os.path.join(storeDir, 'rx', r.msgHash)
            try:
                os.unlink(msgpath)
            except:
                self.log(3, "dbDelIdentity: failed to delete msg file %s" % r.msgHash)

    db.rxMessages.removeWhen(idAddr=idAddr)

    # purge is complete - like, who was fred@blahblah.freemail anyway?

    self.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T106">def dbAddPeer(self, peerAddr):
    """
    Adds new peer record
    """
    db = self.db

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    try:
        uri = freemailAddrToUri(peerAddr)
        db.peers[peerAddr] = cell(peerAddr=peerAddr,
                                  peerUriSite=str(uri),
                                  peerNextSloti=0,
                                  )
        self.dbSave()
    
    except:
        self.log("Error adding peer '%s'\n%s" + (peerAddr, exceptionString()))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

</t>
<t tx="T107">def dbDelPeer(self, peerAddr):
    """
    Deletes a peer from the system.

    Radical action - deletes everything to do with that peer
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    del self.peers[peerAddr]

    self.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------


</t>
<t tx="T108">def dbDelTxMsg(self, msgHash):
    """
    Deletes a message from send queue, and from filesystem
    """
    # get handy things
    db = self.db
    config = db.config

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    for c in [db.txNotSent, db.txNoReceipt, db.txSent, db.txDead]:

        # retrieve matching database records
        vMsgs = c.select(msgHash=msgHash)
    
        # delete the files one by one
        for rMsg in vMsgs:
            filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
            try:
                os.unlink(filename)
            except:
                pass
    
        # and purge from database
        c.removeWhen(msgHash=msgHash)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------</t>
<t tx="T109">def dbDelRxMsg(self, msgHash):
    """
    Deletes a message from receive queue, and from filesystem
    """
    # get handy things
    db = self.db
    config = db.config

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # retrieve matching database records
    vMsgs = db.rxMessages.select(msgHash=msgHash)

    # delete the files one by one
    for rMsg in vMsgs:
        filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
        try:
            os.unlink(filename)
        except:
            pass

    # and purge from database
    db.rxMessages.removeWhen(msgHash=msgHash)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T110">def dbGetTxMsgBody(self, msgHash):
    """
    Given a message hash, return a string of the full message body
    """
    filename = os.path.join(self.db.config.storeDir, "tx", msgHash)
    fd = open(filename, "rb")
    raw = fd.read()
    fd.close()
    return raw
</t>
<t tx="T111">def dbGetRxMsgBody(self, msgHash):
    """
    Given a message hash, return a string of the full message body
    """
    filename = os.path.join(self.db.config.storeDir, "rx", msgHash)
    fd = open(filename, "rb")
    raw = fd.read()
    fd.close()
    return raw
</t>
<t tx="T112"></t>
<t tx="T113">def log(self, level, msg, nPrev=0):
    """
    Logs a message if level &lt;= self.verbosity
    """
    if level &lt;= self._verbosity:

        self.loglock.acquire()
        try:
            fd = open(self.logfile, "ab")
            caller = traceback.extract_stack()[-(2+nPrev)]
            full = "%s:%s:%s():\n* %s" % (caller[0], caller[1], caller[2], msg.replace("\n", "\n   + "))
            now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time()))
            msg = "%s %s\n" % (now, full)
            fd.write(msg)
            fd.close()
            if not self.isQuiet:
                sys.stdout.write(msg)

        except:
            traceback.print_exc()
        self.loglock.release()

</t>
<t tx="T114">def logFreenet(self, level, msg, nPrev=0):
    """
    Logs a message if level &lt;= self.verbosity
    """
    if level &lt;= self._freenetverbosity:

        self.loglock.acquire()
        try:
            fd = open(self.logfile, "ab")
            caller = traceback.extract_stack()[-(2+nPrev)]
            full = "%s:%s:%s():\n* %s" % (caller[0], caller[1], caller[2], msg.replace("\n", "\n   + "))
            now = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time()))
            msg = "%s %s\n" % (now, full)
            fd.write(msg)
            fd.close()
            if not self.isQuiet:
                sys.stdout.write(msg)

        except:
            traceback.print_exc()
        self.loglock.release()

</t>
<t tx="T115">def verbosity(self, level):
    """
    Sets logging verbosity
    
    0 - STFU, except for critical messages
    1 - Important messages
    2 - More info
    3 - Even more info
    4 - Debugging only
    """
    self._verbosity = level
    freenet.verbosity(level-1)
</t>
<t tx="T116"></t>
<t tx="T117">def forceIdRefresh(self, idAddr=None):
    """
    Utility method to force the insertion of an identity's
    mailsite.
    
    Arguments:
        - idAddr - address of identity to reinsert. If this argument
          is not given, then all identities will be reinserted
    
    Returns:
        - none
    
    Note - this just schedules the selected id(s) for reinsertion.
    Watch the log output to see the refreshes happening.
    """
    identities = self.db.identities
    idnames = identities.keys()
    log = self.log
    
    if idAddr != None:
        if idAddr in idnames:
            ids = [idAddr]
        else:
            log(1, "Tried to force refresh of nonexistent id\nidAddr=%s" % idAddr)
            return
    else:
        ids = idnames
    
    for id in ids:
        log(3, "Forcing reinsertion of mail id %s" % id)
        identities[id].idLastInserted = 0

    self.dbSave()

</t>
<t tx="T118">def Node(self, mode='get', **kw):
    """
    Convenience method which creates a freenet/entropy node object, based on
    the mailserver's current configuration

    Arguments:
     - mode - 'put' or 'get', depending on whether this node object will be used
       for inserting or retrieving keys. Determines which htl value in the config
       we use.
    """

    db = self.db
    config = db.config

    if kw.has_key('htl'):
        htl = kw['htl']
    else:
        if mode == 'get':
            htl = config.htlReceive
        else:
            htl = config.htlSend

    return freenet.node(config.fcpHost, config.fcpPort, htl)
</t>
<t tx="T119">def decryptAndVerify(self, ciphertext, keySender, keyRecipient):
    """
    Decrypts message using a local identity's key,
    then verifies the appended signature against the
    remote peer's public key.

    Arguments:
     - msg - raw ciphertext of message. The message is created by
       appending the signature to the end of the plaintext (not even
       a newline), then encrypting it to ascii-armour.
     - keySender - an SSLCrypto key object containing the public key
       of the sender
     - keyRecipient - an SSLCrypto key object containing the public
       and private keys of the recipient

    Returns:
     - plaintext of message, if decryption was successful and the signature was verified
     - None if any kind of failure occurred.

    Possible causes of failure:
     - keySender or keyRecipient are not valid key objects
     - text cannot be decrypted
     - signature is invalid
    """

    # step 1 - check key objects
    if keySender.__class__ != SSLCrypto.key:
        self.log(3, "decryptAndVerify: got invalid sender key")
        return None
    if keyRecipient.__class__ != SSLCrypto.key:
        self.log(3, "decryptAndVerify: got invalid recipient key")
        return None

    # step 2 - decrypt ciphertext
    try:
        plainPlusSig = keyRecipient.decStringFromAscii(ciphertext)
    except:
        self.log(3, "decryptAndVerify: failed to decrypt ciphertext")
        return None

    # step 3 - split into plaintext plus signature
    try:
        plain, sig = plainPlusSig.split("&lt;StartPycryptoSignature&gt;")
        sig = "&lt;StartPycryptoSignature&gt;" + sig
    except:
        self.log(3, "decryptAndVerify: message appears not to contain signature")
        return None

    # step 4 - verify signature against sender key
    try:
        valid = keySender.verifyString(plain, sig)
    except:
        valid = 0
    if not valid:
        self.log(3, "decryptAndVerify: message decrypted, but signature doesn't check out")
        return None

    # success
    return plain

</t>
<t tx="T120">def signAndEncrypt(self, plainText, keySender, keyRecipient):
    """
    Signs message using a local identity's key,
    appends signature to message, then encrypts against the
    remote peer's public key.

    Arguments:
     - plainText - string - raw plaintext of message.
     - keySender - an SSLCrypto key object containing the public key
       and private keys of the local sender
     - keyRecipient - an SSLCrypto key object containing the public
       key of the remote recipient

    Returns:
     - ciphertext, if the keys were valid and the message was successfully
       signed and encypted
     - None if any kind of failure occurred.

    Possible causes of failure:
     - keySender or keyRecipient are not valid key objects
    """

    # step 1 - check key objects
    if keySender.__class__ != SSLCrypto.key:
        self.log(3, "signAndEncrypt: got invalid sender key")
        return None
    if keyRecipient.__class__ != SSLCrypto.key:
        self.log(3, "signAndEncrypt: got invalid recipient key")
        return None

    # step 2 - sign message
    try:
        plainText = str(plainText)
        sig = keySender.signString(plainText)
        signedText = plainText + sig
    except:
        self.log(3, "signAndEncrypt: failed to sign plaintext")
        return None

    # step 3 - encrypt message
    try:
        cipherText = keyRecipient.encStringToAscii(signedText)
    except:
        self.log(3, "decryptAndVerify: failed to encrypt signed message")
        return None

    # success
    return cipherText
</t>
<t tx="T121"></t>
<t tx="T122"></t>
<t tx="T123">class WebUI:
    """
    Handles each incoming HTTP hist
    """
    @others
</t>
<t tx="T124">def __init__(self, req):
    """
    arg 'req' is a handle to an HTTPRequestHandler object
    """

    # set up the bare-bones page
    page = http(stream=req.wfile, streamin=req.rfile)
    self.page = page
    self.setCSS()

    page.title = "Freemail (build %s) Administration" % build

    session = page.session
    self.session = session

    fields = session.fields
    self.fields = fields

    owner = req.owner
    self.owner = owner
    db = owner.db
    self.db = db
    config = db.config
    self.config = config
    
    self._dbLock = owner._dbLock
    self._dbUnlock = owner._dbUnlock


    self.req = req

    self.log = req.log

    # do the interaction
    self.run()

    page.send()
    req.wfile.write("\r\n")

</t>
<t tx="T125">def run(self):

    #set_trace()

    page = self.page
    session = self.session
    fields = self.fields

    db = self.db
    config = self.config
    req = self.req
    owner = self.owner

    #owner.log(4, "top of method")

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    configState = config.configState

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    #owner.log(4, "ok1")

    if configState != 'ready':
        self.wizard(configState)
        return
    
    if not self.isLoggedIn():
        self.showLoginPage()
        return

    page.add(center(h3("Freemail Administration (build %s) Web Interface" % build),
                    ))

    # decide what page to show
    showpage = fields.get('showpage', 'main')
    #owner.log(4, "ok2")
    method = getattr(self, 'page_'+showpage)
    #owner.log(4, "ok3")
    method()

    # temporary - allow HTTP environment dumps
    if session.env['SCRIPT_NAME'].endswith("/env"):
        self.envDump()

</t>
<t tx="T126">def setCSS(self):
    """
    Adds the CSS stylesheet to the page
    """
    page = self.page

    style = self.page.style

    cellcolor = "#ffffc0"
    bgcolor = "#ffffc0"
    tablecolor = "#ffffc0"
    textcolor = "#008000"
    font =  'arial, helvetica, sans-serif'
    borderwidth = '0'
    linktextcolor = bgcolor
    linkbgcolor = textcolor
    linkborderwidth = "thick"
    linkbordercolor = textcolor

    btntextcolor = bgcolor
    btnbgcolor = textcolor
    btnborderwidth = "thick"
    btnbordercolor = textcolor
    btntextsize = "larger"

    fldBgColor = bgcolor
    fldColor = textcolor
    fldFont = font

    style['body'] = 'background:'+bgcolor + ';color:'+textcolor + ';font-family:'+font
    style['table.block'] = 'background:'+cellcolor + ';border-width:'+borderwidth
    style['table.main'] = 'background:'+tablecolor + ';border-width:'+borderwidth
    style['table'] = 'background:'+cellcolor + ';border-width:'+borderwidth
    style['.block'] = 'background:'+cellcolor + ';border-width:'+borderwidth
    style['.mainblock'] = 'background:'+cellcolor +';border-width:'+borderwidth + ';height:100%'
    style['a'] = 'text-decoration:none' + ';color:'+linktextcolor + ';background-color:'+linkbgcolor
    style['a:hover'] = 'text-decoration:none' + ';color:'+linkbgcolor + ';background-color:'+linktextcolor

    style['.textbutton'] = 'text-decoration:none' \
                           + ';color:'+btntextcolor \
                           + ';background-color:'+btnbgcolor \
                           + ';padding: 2px 2px' \
                           + ':border-width: medium' \
                           + ':border-color:'+btnbordercolor # + ';font-size:'+btntextsize

    style['input'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
    style['option'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
    style['select'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
</t>
<t tx="T127"></t>
<t tx="T128">def page_main(self):

    #set_trace()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    fcpHost = config.fcpHost
    fcpPort = config.fcpPort

    host = req.headers.getheader("Host").split(":")[0]

    # launch engine if required
    if fields['startEngine']:
        #print "startEngine: called"
        owner.running = 1
        owner.startFcpEngine()

    if 0 and not fields['shownow']:
        page.add(center("Updating status screen - please wait..."))
        url = "http://%s:%s?showpage=main&amp;shownow=1" % (host, config.httpPort)
        page.head.add('&lt;meta HTTP-EQUIV="refresh" content="1; URL=%s"&gt;\n' % url)
        #page.head.add(script('location.href="%s"' % url))

        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        return
        
    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    # node status
    n = freenet.node(fcpHost, fcpPort)
    try:
        n._handshake()
        systemStatus = 'Online'
    except:
        systemStatus = 'Offline'

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # number of identities
    numIdentities = len(db.identities.keys())

    # peers
    numPeers = len(db.peers)

    # inbound messages
    numUnreadMessages = len(db.rxMessages.select(isDeleted=0))
    numUnsentMessages = len(db.txNotSent)
    numUnconfirmedMessages = len(db.txNoReceipt)
    numConfirmedMessages = len(db.txSent)
    numDeadMessages = len(db.txDead)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    if owner.running:
        tdEngine = notag(td("Running"), td())
    else:
        tdEngine = notag(td("Not Running"),
                         td(btnForm("Start Freemail Engine", showpage="main", startEngine=1)))
    
    page.add(center(table(attr(cellspacing=0, cellpadding=3),
                          tr(td(b("Freemail Engine Status: ")),
                             tdEngine,
                             ),
                          tr(td(b("Freenet/Entropy Node Status: ")),
                             td(systemStatus),
                             td(btnForm("Configure System", showpage="configureSystem")),
                             ),
                          tr(td(b("Number of Mailing Identities: ")),
                             td(numIdentities),
                             td(btnForm("Manage Identities", showpage="manageIdentities")),
                             ),
                          tr(td(b("Number of Unretrieved Messages: ")),
                             td(numUnreadMessages),
                             #td(btnForm("Manage Inbound Messages", showpage="manageInbound")),
                             ),
                          tr(td(b("Number of Unsent Messages: ")),
                             td(numUnsentMessages),
                             #td(btnForm("Manage Outbound Messages", showpage="manageOutbound")),
                             ),
                          tr(td(b("Number of Unconfirmed Messages: ")),
                             td(numUnconfirmedMessages),
                             #td(btnForm("Manage Unconfirmed Messages", showpage="manageOutbound")),
                             ),
                          tr(td(b("Number of Confirmed Messages: ")),
                             td(numConfirmedMessages),
                             #td(btnForm("Manage Confirmed Messages", showpage="manageOutbound")),
                             ),
                          tr(td(b("Number of Dead Messages: ")),
                             td(numDeadMessages),
                             #td(btnForm("Manage Dead Messages", showpage="manageOutbound")),
                             ),
                          tr(td(attr(colspan=3, align='center'),
                                table(attr(cellspacing=0, cellpadding=3, border=0, align='center'),
                                      tr(td(btnForm("Refresh", showpage="main")),
                                         td(btnForm("Logout", "/logout")),
                                         ),
                                      ),
                                ),
                             ),
                          )))



</t>
<t tx="T129">def page_configureSystem(self):

    #set_trace()

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    savedMsg = ''
    errors = ''

    # get values from database
    adminLogin = config.adminLogin
    adminPassword = ''
    adminPassword1 = ''
    fcpHost = config.fcpHost
    fcpPort = config.fcpPort
    httpPort = config.httpPort
    popPort = config.popPort
    telnetPort = config.telnetPort
    smtpPort = config.smtpPort
    adminTimeout = config.adminTimeout
    htlSend = config.htlSend
    htlReceive = config.htlReceive

    try:
        receiptAll = config.receiptAll
    except:
        receiptAll = 0
        config.receiptAll = receiptAll
        owner.dbSave()


    if receiptAll:
        tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes" CHECKED&gt;'
    else:
        tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes"&gt;'

    storeDir = config.storeDir
    rxMaxRetries = config.rxMaxRetries
    txMaxRetries = config.txMaxRetries
    getMaxRegress = config.getMaxRegress
    cryptoKeySize = config.cryptoKeySize
    slotLookAhead = config.slotLookAhead
    txBackoffInit = config.txBackoffInit
    txBackoffMult = config.txBackoffMult

    httpHosts = owner.db.httpHosts
    popHosts = owner.db.popHosts
    smtpHosts = owner.db.smtpHosts
    telnetHosts = owner.db.telnetHosts

    cryptoKeySel = {1024:'', 2048:'', 3072:'', 4096:''}
    cryptoKeySel[cryptoKeySize] = " selected"

    # are we trying to save?
    if fields['command'] == 'save':
        # grab all the values off the form
        adminLogin = fields['adminLogin']
        adminPassword = fields['adminPassword']
        adminPassword1 = fields['adminPassword1']
        fcpHost = fields['fcpHost']
        fcpPort = fields['fcpPort']
        try:
            fcpPort = int(fcpPort)
        except:
            pass
        httpPort = fields['httpPort']
        try:
            httpPort = int(httpPort)
        except:
            pass
        popPort = fields['popPort']
        try:
            popPort = int(popPort)
        except:
            pass
        smtpPort = fields['smtpPort']
        try:
            smtpPort = int(smtpPort)
        except:
            pass
        telnetPort = fields['telnetPort']
        try:
            telnetPort = int(telnetPort)
        except:
            pass
        adminTimeout = fields['adminTimeout']
        try:
            adminTimeout = int(adminTimeout)
        except:
            pass
        htlSend = fields['htlSend']
        try:
            htlSend = int(htlSend)
        except:
            pass
        htlReceive = fields['htlReceive']
        try:
            htlReceive = int(htlReceive)
        except:
            pass

        receiptAll = (fields['receiptAll'] == 'yes')
        if receiptAll:
            tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes" CHECKED&gt;'
        else:
            tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes"&gt;'
        
        storeDir = fields['storeDir']

        rxMaxRetries = fields['rxMaxRetries']
        try:
            rxMaxRetries = int(rxMaxRetries)
        except:
            pass

        txMaxRetries = fields['txMaxRetries']
        try:
            txMaxRetries = int(txMaxRetries)
        except:
            pass
        getMaxRegress = fields['getMaxRegress']
        try:
            getMaxRegress = int(getMaxRegress)
        except:
            pass
        cryptoKeySel = {1024:'', 2048:'', 3072:'', 4096:''}
        cryptoKeySize = fields['cryptoKeySize']
        try:
            cryptoKeySize = int(cryptoKeySize)
            cryptoKeySel[cryptoKeySize] = " selected"
        except:
            pass

        slotLookAhead = fields['slotLookAhead']
        try:
            slotLookAhead = int(slotLookAhead)
        except:
            pass
        txBackoffInit = fields['txBackoffInit']
        try:
            txBackoffInit = int(txBackoffInit)
        except:
            pass
        txBackoffMult = fields['txBackoffMult']
        try:
            txBackoffMult = float(txBackoffMult)
        except:
            pass

        httpHosts = [h.strip() for h in fields['httpHosts'].split(",")]
        smtpHosts = [h.strip() for h in fields['smtpHosts'].split(",")]
        popHosts = [h.strip() for h in fields['popHosts'].split(",")]
        telnetHosts = [h.strip() for h in fields['telnetHosts'].split(",")]

        # now validate them
        problems = []
        if adminLogin == '':
            problems.append("You must enter a username")
        if adminPassword or adminPassword1:
            if adminPassword == '':
                problems.append("You must enter a password")
            elif adminPassword != adminPassword1:
                problems.append("Your passwords do not match")
            elif len(password) &lt; 6:
                problems.append("Your password is too short")

        if fcpHost == '':
            problems.append("You must specify an FCP host")

        if not fcpPort:
            problems.append("You must specify an FCP port")
        elif type(fcpPort) is not type(0):
            problems.append("FCP Port must be a number")
        elif fcpPort &lt; 1 or fcpPort &gt; 65535:
            problems.append("Invalid FCP Port '%s' - should be between 1 and 65535" % fcpPort)

        if not httpPort:
            problems.append("You must specify an HTTP port")
        elif type(httpPort) is not type(0):
            problems.append("HTTP Port must be a number")
        elif httpPort &lt; 1 or httpPort &gt; 65535:
            problems.append("Invalid HTTP Port '%s' - should be between 1 and 65535" % httpPort)

        if not popPort:
            problems.append("You must specify a POP3 port")
        elif type(popPort) is not type(0):
            problems.append("POP3 Port must be a number")
        elif popPort &lt; 1 or popPort &gt; 65535:
            problems.append("Invalid POP3 Port '%s' - should be between 1 and 65535" % popPort)

        if not smtpPort:
            problems.append("You must specify an SMTP port")
        elif type(smtpPort) is not type(0):
            problems.append("SMTP Port must be a number")
        elif smtpPort &lt; 1 or smtpPort &gt; 65535:
            problems.append("Invalid SMTP Port '%s' - should be between 1 and 65535" % smtpPort)

        if not telnetPort:
            problems.append("You must specify a Telnet port")
        elif type(telnetPort) is not type(0):
            problems.append("Telnet Port must be a number")
        elif telnetPort &lt; 1 or telnetPort &gt; 65535:
            problems.append("Invalid Telnet Port '%s' - should be between 1 and 65535" % telnetPort)

        if not adminTimeout:
            problems.append("You must specify an admin timeout")
        elif type(adminTimeout) is not type(0):
            problems.append("Admin timeout must be a number")
        elif adminTimeout &lt; 60:
            problems.append("Dumb value for admin timeout - "
                            "please try at least 60 (recommend 3600)")
        if not htlSend and type(htlSend) != type(0):
            problems.append("You must specify an HTL value for sending")
        elif type(htlSend) is not type(0):
            problems.append("HTL TX value must be a number")
        elif htlSend  &gt; 100:
            problems.append("Dumb HTL TX value '%s' - try something like 20" % htlSend)
        if not htlReceive and type(htlReceive) != type(0):
            problems.append("You must specify an HTL value for receiving")
        elif type(htlReceive) is not type(0):
            problems.append("HTL RX value must be a number")
        elif htlReceive  &gt; 100:
            problems.append("Dumb HTL RX value '%s' - try something like 20" % htlReceive)
        if not storeDir:
            problems.append("You must choose a datastore directory")
        else:
            if os.path.isdir(storeDir):
                # try to set permissions
                try:
                    os.chmod(storeDir, 0700)
                except:
                    problems("Failed to set permissions for store directory '%s' "
                             "please choose another path" % storeDir)
            else:
                # try to create store directory now
                try:
                    os.makedirs(storeDir, 0700)
                except:
                    problems.append("Failed to create store directory '%s' "
                                    "please try something else" % storeDir)

        if not rxMaxRetries:
            problems.append("You must specify a maximum receive retry count")
        elif type(rxMaxRetries) is not type(0):
            problems.append("receive retry count should be a number")
        elif rxMaxRetries not in range(1, 20):
            problems.append("Invalid receive retry limit '%s' - "
                            "please choose something between 1 and 20" % rxMaxRetries)

        if not txMaxRetries:
            problems.append("You must specify a maximum transmit retry count")
        elif type(txMaxRetries) is not type(0):
            problems.append("transmit retry count should be a number")
        elif txMaxRetries not in range(1, 29):
            problems.append("Invalid transmit retry limit '%s' - "
                            "please choose something between 1 and 28" % txMaxRetries)
        elif getMaxRegress not in range(1, 20):
            problems.append("Invalid regress limit '%s' - "
                            "please choose something between 1 and 28" % getMaxRegress)
        elif cryptoKeySize not in [1024, 2048, 3072, 4096]:
            problems.append("Bad crypto key size '%s' - choose 1024, 2048, 3072 or 4096" % cryptoKeySize)

        if not slotLookAhead:
            problems.append("You must specify an initial rx slot look-ahead count")
        elif type(slotLookAhead) is not type(1):
            problems.append("RX Slot look-ahead should be a number, best 2-10")
        elif slotLookAhead &lt; 1 or slotLookAhead &gt; 10:
            problems.append("Crazy RX slot look-ahead '%s' - "
                            "please choose something between 1 and 10 (recommend 5)" % slotLookAhead)

        if not txBackoffInit:
            problems.append("You must specify an initial tx backoff interval")
        elif type(txBackoffInit) is not type(1):
            problems.append("initial send backoff interval should be a number, best 7200 or more")
        elif txBackoffInit &lt; 300 or txBackoffInit &gt; 86400:
            problems.append("Crazy initial send backoff interval '%s' - "
                            "please choose something between 1800 and 86400" % txBackoffInit)

        if not txBackoffMult:
            problems.append("You must specify a send backoff multiplier")
        elif type(txBackoffMult) not in [type(1), type(1.1)]:
            problems.append("Send backoff multiplier should be a number from 1.1 to 4.0")
        elif txBackoffMult &lt; 1.1 or txBackoffMult &gt; 4.0:
            problems.append("Crazy send backoff multiplier '%s' - "
                            "please choose something between 1.1 and 4.0" % txBackoffMult)

        if not httpHosts or httpHosts == ['']:
            httpHosts = ['127.0.0.1']
        if not smtpHosts or smtpHosts == ['']:
            smtpHosts = ['127.0.0.1']
        if not popHosts or popHosts == ['']:
            popHosts = ['127.0.0.1']
        if not telnetHosts or telnetHosts == ['']:
            telnetHosts = ['127.0.0.1']

        if problems:
            errors = notag("Sorry, but there were errors:", ulFromList(problems))

        else:
            # write new values to database
            config.adminLogin = adminLogin
            if adminPassword:
                config.adminPassword = hash(adminPassword)
            config.fcpHost = fcpHost
            config.fcpPort = fcpPort
            config.popPort = popPort
            config.smtpPort = smtpPort
            config.httpPort = httpPort
            config.telnetPort = telnetPort
            config.adminTimeout = adminTimeout
            config.htlSend = htlSend
            config.htlReceive = htlReceive
            config.receiptAll = receiptAll
            config.storeDir = storeDir
            config.rxMaxRetries = rxMaxRetries
            config.txMaxRetries = txMaxRetries
            config.getMaxRegress = getMaxRegress
            config.cryptoKeySize = cryptoKeySize
            config.slotLookAhead = slotLookAhead
            config.txBackoffInit = txBackoffInit
            config.txBackoffMult = txBackoffMult

            # now the allowed hosts
            owner.db.httpHosts = httpHosts
            owner.db.smtpHosts = smtpHosts
            owner.db.popHosts = popHosts
            owner.db.telnetHosts = telnetHosts

            owner.dbSave()
            savedMsg = center(i("Changes saved successfully"))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    page.add(center(h3("System Configuration")))
    page.add(center(btnForm("Return to Main Page", showpage="main")))

    page.add(center(table(attr(align='center', cellspacing=0, cellpadding=2),
                          tr(td(attr(colspan=2),
                                errors, savedMsg)),
                          form(attr(method="POST"),
                               input(type="hidden", name="showpage", value="configureSystem"),
                               input(type="hidden", name="command", value="save"),

                               # ---------------------------------------
                               tr(td(attr(colspan=2, align='center'),
                                     hr(),
                                     b(big("Security Settings")),
                                     )),
                               tr(td(b("Admin Interface Login: ")),
                                  td(input(type="text", name="adminLogin", value=adminLogin))),
                               tr(td(b("Password: "),
                                     br(),
                                     small(i("(Leave blank to keep existing password)")),
                                     ),
                                  td(input(type="text", name="adminPassword", value=""))),
                               tr(td(b("Confirm Password: ")),
                                  td(input(type="text", name="adminPassword1", value=""))),

                               # ---------------------------------------
                               tr(td(attr(colspan=2, align='center'),
                                     hr(),
                                     b(big("Server Settings")),
                                     )),
                               tr(td(b("POP3 Port: ")),
                                  td(input(type="text", name="popPort", size=6, maxlen=5, value=popPort))),
                               tr(td(b("Allowed POP3 hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="popHosts", size=32, maxlen=80, value=",".join(popHosts))),
                                  ),

                               tr(td(b("SMTP Port: ")),
                                  td(input(type="text", name="smtpPort", size=6, maxlen=5, value=smtpPort))),
                               tr(td(b("Allowed SMTP hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="smtpHosts", size=32, maxlen=80, value=",".join(smtpHosts))),
                                  ),

                               tr(td(b("Web Interface Port: ")),
                                  td(input(type="text", name="httpPort", size=6, maxlen=5, value=httpPort))),
                               tr(td(b("Web Interface Timeout: "), br(),
                                     small(i("(seconds)"))),
                                  td(input(type="text", name="adminTimeout", size=8, maxlen=6, value=adminTimeout)),
                                  ),
                               tr(td(b("Allowed HTTP hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="httpHosts", size=32, maxlen=80, value=",".join(httpHosts))),
                                  ),

                               tr(td(b("Telnet Port: ")),
                                  td(input(type="text", name="telnetPort", size=6, maxlen=5, value=telnetPort))),
                               tr(td(b("Allowed Telnet hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="telnetHosts",
                                           size=32, maxlen=80, value=",".join(telnetHosts))),
                                  ),

                               # ---------------------------------------
                               tr(td(attr(colspan=2, align='center'),
                                     hr(),
                                     b(big("Freenet/Entropy Node Settings")),
                                     )),
                               tr(td(b("FCP Host: ")),
                                  td(input(type="text", name="fcpHost", size=32, maxlen=32, value=fcpHost))),
                               tr(td(b("FCP Port: ")),
                                  td(input(type="text", name="fcpPort", size=6, maxlen=5, value=fcpPort))),
                               tr(td(b("Hops To Live (sending): "),
                                     ),
                                  td(input(type="text", name="htlSend", size=3, maxlen=3, value=htlSend)),
                                  ),
                               tr(td(b("Hops To Live (receiving): "),
                                     ),
                                  td(input(type="text", name="htlReceive", size=3, maxlen=2, value=htlReceive)),
                                  ),

                               # ---------------------------------------
                               tr(td(attr(colspan=2, align='center'),
                                     hr(),
                                     b(big("Message Transport Settings")),
                                     )),
                               tr(td(b("Issue receipts for all sent messages?")),
                                  td(tagReceiptAll),
                                  ),

                               tr(td(b("Message Store Directory: "),
                                     ),
                                  td(input(type="text", name="storeDir", size=32, value=storeDir)),
                                  ),

                               tr(td(b("Receive Retry Count: "),
                                     ),
                                  td(input(type='text', name='rxMaxRetries', size=6, value=rxMaxRetries),
                                     )
                                  ),

                               tr(td(b("Maximum Retries for Sending: ")),
                                  td(input(type="text", name="txMaxRetries", size=3, maxlen=2, value=txMaxRetries)),
                                  ),
                               tr(td(b("Maximum Regress for peer site retrievals: ")),
                                  td(input(type="text", name="getMaxRegress", size=3, maxlen=2, value=getMaxRegress)),
                                  ),
                               tr(td(attr(align='left'),
                                     b("Encryption Key Size: "),
                                     ),
                                  td('',
                                     #input(type='text', name='cryptoKeySize', size=6, value=cryptoKeySize),
                                     '&lt;select name="cryptoKeySize"&gt;',
                                     '&lt;option label="1024 bits (low grade)" value="1024" %s&gt;'
                                       '1024 bits (low grade)&lt;/option&gt;' % cryptoKeySel[1024],
                                     '&lt;option label="2048 bits (personal grade)", value="2048" %s&gt;'
                                       '2048 bits (personal grade)&lt;/option&gt;' % cryptoKeySel[2048],
                                     '&lt;option label="3072 bits (commercial grade)", value="3072" %s&gt;'
                                       '3072 bits (commercial grade)&lt;/option&gt;' % cryptoKeySel[3072],
                                     '&lt;option label="4096 bits (military grade)", value="4096" %s&gt;'
                                       '4096 bits (military grade)&lt;/option&gt;' % cryptoKeySel[4096],
                                     "&lt;/select&gt;",
                                     ),
                                  ),
                               tr(td(b("Receive Slot Look-Ahead: "),
                                     ),
                                  td(input(type='text', name='slotLookAhead', size=3, maxlen=2, value=slotLookAhead)),
                                  ),
                               tr(td(b("Initial Send Retry Interval: "), br(),
                                     small(i("(seconds)"))
                                     ),
                                  td(input(type='text', name='txBackoffInit', size=7, maxlen=6, value=txBackoffInit)),
                                  ),
                               tr(td(b("Send Retry Multiplier: "), br(),
                                     small(i("(float)"))
                                     ),
                                  td(input(type='text', name='txBackoffMult', size=5, maxlen=4, value=txBackoffMult)),
                                  ),
                               tr(td(attr(colspan=2, align="center"),
                                     hr(),
                                     btn("Save Changes")),
                                  ),
                              )
                          )
                    )
             )


</t>
<t tx="T130">def page_manageIdentities(self):

    #set_trace()

    #self.log(4, "top of method")

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    try:
        page = self.page
        session = self.session
        fields = self.fields
        req = self.req

        owner = self.owner
        db = owner.db
        config = db.config
    
        page.add(center(h3("Manage Mailing Identities")))
        page.add(center(table(attr(cellspacing=0, cellpadding=0, border=0),
                              tr(td(btnForm("Return to Main Page", showpage="main")),
                                 td(btnForm("Create New Identity", showpage="createIdentity")),
                                 )
                              )
                        )
                 )
    
        # pick up any delete or delete confirmation command
        cmd = fields['cmd']

        if cmd == 'deleteOk':
            #self.log(2, "ok1")
            idAddr = fields['idAddr']
            #self.log(2, "ok2")
            db = owner.db
            #self.log(2, "ok3")
            if db.identities.has_key(idAddr):
                #self.log(2, "ok4")
                owner.dbDelIdentity(idAddr)
                #self.log(2, "ok5")
                page.add(br(), br(),
                         center(i("Identity "), br(),
                                b(idAddr), br(),
                                i(" was purged completely. Kaput.", br(),
                                  "If there was any sensitive information, however, please be warned "
                                  "that a forensic audit of your PC can easily recover the files. So "
                                  "you may want to run a disk-cleaner program that wipes off all free space.",
                                  )),
                         )
            #self.log(2, "ok6")
    
        elif cmd == 'delete':
            idAddr = fields['idAddr']
            page.add(center(table(attr(width='60%', cellspacing=0, cellpadding=3, border=0),
                                  tr(td(attr(colspan=2, align='center'),
                                        br(),
                                        big(big(b("Warning"))), br(),
                                        "you are about to delete your own mailing identity", br(),
                                        big(b(idAddr)), br(),
                                        "from your Freemail database.", br(),
                                        "This will permanently and irrecoverably delete the identity, "
                                        "as well as all record of any messages "
                                        "involving this identity.", br(), br(),
                                        "Are you really sure you want to do this?")),
                                  tr(td(attr(align='right'),
                                        btnForm("Yes",
                                                showpage='manageIdentities',
                                                cmd='deleteOk',
                                                idAddr=idAddr)),
                                     td(attr(align='left'),
                                        btnForm("No",
                                                showpage='manageIdentities'))))))
            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return
    
        # display a listing of all active mailing identities
        db = self.owner.db
        vIds = db.identities.values()
        vPeers = db.peers
        vRxMsgs = db.rxMessages

        tIds = notag()
        tMain = table(attr(align='center', cellspacing=0, cellpadding=3, border=1),
                      tr(td(attr(align='center', colspan=1),
                            b("Your Mailing Identities")
                            )),
                      br(), br(),
                      tIds,
                      )
        page.add(tMain)

        if not vIds:
            tIds.add(tr(td(i("(No identities currently defined)"))))

        # populate with our identities
        for rId in vIds:
            idAddr = rId.idAddr

            vTxNotSent = db.txNotSent.select(idAddr=idAddr)
            vTxNoReceipt = db.txNoReceipt.select(idAddr=idAddr)
            vTxSent = db.txSent.select(idAddr=idAddr)
            vTxDead = db.txDead.select(idAddr=idAddr)

            nTxNotSent = len(vTxNotSent)
            nTxNoReceipt = len(vTxNoReceipt)
            nTxSent = len(vTxSent)
            nTxDead = len(vTxDead)
            nTxTotal = nTxNotSent + nTxNoReceipt + nTxDead
    
            vRxI = vRxMsgs.select(idAddr=idAddr)
            nRxMsgsTotal = len(vRxI)
            nRxMsgsUnread = len(vRxI.select(isDeleted=0))

            tIds.add(tr(td(table(attr(cellspacing=0, cellpadding=0, border=0),
                                 tr(td(b("FreeMail address::&amp;nbsp;")),
                                    td(idAddr)),
                                 tr(td(b("Stored in Freenet as:&amp;nbsp;")),
                                    td(rId.idUriSite+"//")),
                                 tr(td(b("SSK Public Key:")),
                                    td(rId.idSskPub)),
                                 tr(td(b("SSK Private Key:")),
                                    td(rId.idSskPriv)),
                                 tr(td(b("Receive Status::&amp;nbsp;")),
                                    td("&amp;bull;&amp;nbsp;%d unread messages (total %d received)" % (nRxMsgsUnread, nRxMsgsTotal))),
                                 tr(td(b("Send Status::&amp;nbsp;")),
                                    td("&amp;bull;&amp;nbsp;%d messages sent (%d waiting, %d awaiting receipt, "
                                       "%d confirmed, %d dead)" % (nTxTotal,
                                                                   nTxNotSent,
                                                                   nTxNoReceipt,
                                                                   nTxSent,
                                                                   nTxDead,
                                                                   ),
                                       )),

                                 tr(# td(btnForm('Manage Identity', showpage='manageIdentity', idAddr=idAddr)),
                                    td(attr(colspan=2, align='center'),
                                       btnForm('Delete Identity', showpage='manageIdentities', cmd='delete', idAddr=idAddr)),
                                    ),
                                 ),
                           )))
    
            #print "idAddr=%s" % idAddr
        #print "----"

    except:
        self.log(2, "Some operation failed here:\n%s" % exceptionString())

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T131">def page_createIdentity(self):

    #set_trace()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    log = owner.log

    problems = []
    addproblem = problems.append

    page.add(center(h3("Create New Mailing Identity"),
                    table(attr(cellspacing=0, cellpadding=3, align='center'),
                          tr(td(btnForm("Return to Main Page", showpage="main")),
                             td(btnForm("Return to Identities", showpage='manageIdentities')))),
                    br(),
                    ))

    # parse the HTTP headers to get the hostname used by the browser
    #print req.headers
    #print type(req.headers)
    #nl = re.compile("[\\r\\n]+")
    #headerlines = nl.split(req.headers)
    host = req.headers.getheader("Host").split(":")[0]
    log(3, "page_createIdentity: you reached this web interface as http://%s:%s" % (host, config.httpPort))

    idName = fields['idName']
    sskPub = fields['sskPub']
    sskPriv = fields['sskPriv']
    genssk = fields['genssk']
    keySize = fields['keySize']
    keyAsTxt = fields['keyAsTxt']
    pop3Password = fields['pop3Password']
    pop3Password1 = fields['pop3Password1']
    insertHtl = fields['insertHtl']

    try:
        insertPeriod = int(fields['insertPeriod'])
    except:
        insertPeriod = 86400 # set the mailsite DBR period to 24 hours
        
    cryptoKeyLen = 2048

    cmd = fields.get('cmd', 'new')

    # generate an ssk keypair if needed
    if genssk:
        node = self.owner.Node()
        sskPub, sskPriv = node.genkeypair()
        cmd = 'genssk'

    # processing of different stages is here in reverse order, to allow a 'drop-thru'
    # to previous state in case anything goes wrong. Data from earlier states gets
    # stored as form variables in later states

    if cmd == 'insertid':

        # save in database, and do the insert

        # re-constitute key
        log(2, "re-constituting SSLCrypto key")
        key = SSLCrypto.key(b64dec(keyAsTxt))
        log(2, "SSLCrypto key reconstituted fine")

        # save all in database
        idAddr = owner.dbAddIdentity(idName,
                                     password=pop3Password,
                                     cryptoKey=key,
                                     sskPub=sskPub,
                                     sskPriv=sskPriv,
                                     insertPeriod=insertPeriod,
                                     refreshInProgress=1,
                                     )

        log(2, "new identity for %s added to database" % idName)

        # create node object
        try:
            insertHtl = int(insertHtl)
        except:
            addproblem("HTL must be a number from 0 to 40")
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            problems = []
            cmd = 'generatekey' # drop thru to prev state

        if not problems:
            node = self.owner.Node(htl=insertHtl)

            # stick the mailsite in
            try:
                log(2, "inserting new identity %s into freenet (entropy)" % idName)
                owner.fcpRefreshMailsite(idAddr, htl=insertHtl)
                log(2, "insert of new identity %s successful" % idName)
            except:
                addproblem("Failed to insert into Freenet/Entropy the info for your new identity:\n%s\n%s" % (
                    idAddr, exceptionString()))
                log(2, "New id insert problem:\n%s" % exceptionString())

            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'generatekey' # drop thru to prev state
            else:
                # everything went fine
                page.add(center(b("Success"), br(), br(),
                                "Your new freemail account was successfully created and inserted).",
                                br(), br(),
                                "Your new FreeMail address is:", br(),
                                b(idAddr), br(), br(),
                                "and it has been inserted into Freenet (or Entropy) as:", br(),
                                b(owner.db.identities[idAddr].idUriSite+"//"), br(), br(),

                                b("One last step:"), br(),
                                "Please now create a new mailing account in your normal "
                                "email program", br(),
                                small(i("(Mozilla, Evolution, Mutt, Pine, Kmail, Outlook Express etc)")), br(),
                                "and set it up with the following details:", br(), br(),

                                table(attr(align='center', cellspacing=0, cellpadding=3, border=1),
                                      tr(td(attr(align='center', colspan=2),
                                            b("General Settings:"))),
                                      tr(td(b("Name: ")),
                                         td("%s at FreeMail" % idName)),
                                      tr(td(b("Email Address: ")),
                                         td(attr(valign='top'), idAddr)),
                                      tr(td(attr(align='center', colspan=2),
                                            b("Receive Settings:"))),
                                      tr(td(attr(valign='top'),
                                            b("POP3 Server:")),
                                         td(attr(valign='top'), host)),
                                      tr(td(b("POP3 Port: ")),
                                         td(attr(valign='top'), config.popPort)),
                                      tr(td(b("POP3 Username: ")),
                                         td(attr(valign='top'), idAddr)),
                                      tr(td(b("POP3 Password: ")),
                                         td(attr(valign='top'), pop3Password)),
                                      tr(td(attr(align='center', colspan=2),
                                            b("Send Settings:"))),
                                      tr(td(b("SMTP Server: ")),
                                         td(attr(valign='top'), host)),
                                      tr(td(b("SMTP Port: ")),
                                         td(attr(valign='top'), config.smtpPort)),
                                      ),
                                ))

    if cmd == 'generatekey':

        # Generate a crypto key of required size, notify of insertion
        try:
            keySize = int(keySize)
        except:
            try:
                log(2,
                    "Strange problem - exception determining int(keySize)\n"
                    "keySize='%s'\n"
                    "defaulting to 3072-bit\n"
                    "%s" % (
                         keySize, exceptionString()))
            except:
                log(2,
                    "Even stranger - the local var 'keySize' has gone AWOL!!!\n"
                    "Defaulting to 3072-bit\n"
                    "%s" % exceptionString())
                keySize = 3072
        key = SSLCrypto.key(keySize, 'ElGamal')
        keyAsTxt = b64enc(key.exportKeyPrivate())

        page.add(center(form(attr(method="POST", action="/"),
                             input(type='hidden', name='showpage', value='createIdentity'),
                             input(type='hidden', name='cmd', value='insertid'),
                             input(type='hidden', name='idName', value=idName),
                             input(type='hidden', name='sskPub', value=sskPub),
                             input(type='hidden', name='sskPriv', value=sskPriv),
                             input(type='hidden', name='keyAsTxt', value=keyAsTxt),
                             input(type='hidden', name='pop3Password', value=pop3Password),

                             b("Insert Identity into Freenet"), br(),
                             "Your new mailing identity is ready to be inserted into Freenet (or Entropy)", br(),
                             "Please select a HTL (hops-to-live) parameter for this insertion.", br(),
                             small(i("(Note - the higher this value, the longer it will take now to "
                                     "complete the insertion - but the easier it will be for other "
                                     "FreeMail servers to locate your identity.", br(),
                                     "Feel free to use a low value (even 0) if you are presently testing FreeMail.)",
                                     )), br(), br(),

                             table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                   tr(td(b("Insert HTL")),
                                      td(input(type='text', name='insertHtl', value="15"))),
                                   ),
                                   
                             btn("Insert into Freenet and Continue"), br(),
                             small(i("(Please click this only once)")),

                             )))

    if cmd == 'promptkeysize':

        # validate the POP3 passwords
        if pop3Password == '':
            addproblem("You must choose a password")
        elif pop3Password != pop3Password1:
            addproblem("Passwords do not match")
        if problems:
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            problems = []
            cmd = 'getpassword' # drop thru to prev state
        else:
            # ssk keys are fine
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createIdentity'),
                                 input(type='hidden', name='cmd', value='generatekey'),
                                 input(type='hidden', name='idName', value=idName),
                                 input(type='hidden', name='sskPub', value=sskPub),
                                 input(type='hidden', name='sskPriv', value=sskPriv),
                                 input(type='hidden', name='pop3Password', value=pop3Password),

                                 b("Generate encryption key"), br(), br(),
                                 "How strong do you want the encryption for your incoming messages?", br(),
                                 small(i("(Note - if you choose a large key, and/or if you are running "
                                         "on a slow machine, this key could take a while to generate, "
                                         "so please be patient.)")), br(), br(),
                                 
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(label('&lt;input type="radio", name="keySize", value="512"&gt;',
                                                   "512 bits (weak, for testing only)"))),
                                       tr(td(label('&lt;input type="radio", name="keySize", value="1024"&gt;',
                                                   "1024 bits (personal grade)"))),
                                       tr(td(label('&lt;input type="radio", name="keySize", value="2048" checked&gt;',
                                                   "2048 bits (commercial grade)"))),
                                       tr(td(label('&lt;input type="radio", name="keySize", value="3072"&gt;',
                                                   "3072 bits (military grade)"))),
                                       tr(td(label('&lt;input type="radio", name="keySize", value="4096"&gt;',
                                                   "4096 bits (paranoia grade)"))),
                                       ),

                                 btn("Generate Encryption Key and Continue"), br(),
                                 small(i("(Please click this only once)")),

                                 )))

    if cmd == 'getpassword':

        # prompt for POP3 password
        # validate the SSK keypair
        node = owner.Node(htl=0)
        try:
            pub = node.put("test", "", "SSK@%s/xxxtest-%s" % (sskPriv, random.randint(0, 1000000))).uri.hash
            if pub != sskPub:
                addproblem("The public and private keys do not match")
        except:
            addproblem("Cannot contact node to validate your keypair")
            self.log(1, "Failed to contact node:\n%s" % exceptionString())

        if problems:
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            problems = []
            cmd = 'genssk' # drop thru to prev state
        else:
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createIdentity'),
                                 input(type='hidden', name='cmd', value='promptkeysize'),
                                 input(type='hidden', name='idName', value=idName),
                                 input(type='hidden', name='sskPub', value=sskPub),
                                 input(type='hidden', name='sskPriv', value=sskPriv),
                                 
                                 b("Secure your POP3 Mailbox"), br(), br(),
                                 "Please choose a password, that your mail program will use "
                                 "for retrieving your FreeMail messages via the POP3 port.", br(),br(),
                                 
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(b("POP3 Password: ")),
                                          td(input(type="text", name="pop3Password", value=pop3Password, size=16))),
                                       tr(td(b("Confirm POP3 Password: ")),
                                          td(input(type="text", name="pop3Password1", value=pop3Password1, size=16))),
                                       ),
                                 btn("Continue"), br(),

                                 ),
                            ),
                     )
        
    if cmd == 'genssk':

        # validate chosen name
        lenIdName = len(idName)
        if idName == '':
            addproblem("You did not enter a name")
        elif lenIdName &lt; 3:
            addproblem("Name is too short")
        if lenIdName &gt; 16:
            addproblem("Name is too long")
        if not re.match("^[a-zA-Z0-9\\-]+$", idName):
            addproblem("Name should only consist of letters, digits or hyphens")

        if problems:
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            cmd = 'new' # drop thru to prev state

        else:
            # all ok - stick up ssk keys form
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createIdentity'),
                                 input(type='hidden', name='cmd', value='getpassword'),
                                 input(type='hidden', name='idName', value=idName),

                                 b("Secure your mailing identity"), br(), br(),
                                 "In order to protect your identity against tampering "
                                 "attacks, we will need to secure it with a freenet SSK keypair.",
                                 br(),
                                 small(i("If you have an existing SSK keypair ",
                                         "(eg, one which you are using with an freesite), "
                                         "please enter it here.", br(),
                                         "Otherwise, please select 'Generate New Keys' to "
                                         "generate a whole new keypair.")), br(), br(),

                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(attr(colspan=2, align='center'),
                                             label(input(type='checkbox', name='genssk', value='yes'),
                                                   "Generate new SSK keys"
                                                   ))),
                                       tr(td(b("SSK Public Key: ")),
                                          td(input(type="text", name="sskPub", value=sskPub, size=40))),
                                       tr(td(b("SSK Private Key: ")),
                                          td(input(type="text", name="sskPriv", value=sskPriv, size=40))),
                                       tr(td(attr(colspan=2, align='center'),
                                             center(btn("Continue")))),
                                       )))),
                                       

    if cmd == 'new':
        # first stage - prompt for name, password and keysize
        page.add(center(form(attr(method="POST", action="/"),
                             input(type='hidden', name='showpage', value='createIdentity'),
                             input(type='hidden', name='cmd', value='genssk'),

                             b("Please choose a short name"), br(),
                             small(i("(3-16 chars - letters, digits and '-' only)")),

                             table(attr(cellspacing=0, cellpadding=3, align='center', border=1),
                                   tr(td(input(type="text", name="idName", value=idName, size=18, maxlen=16)),
                                      ),
                                   ),
                             center(btn("Continue")),
                             ),
                        ),
                 )

    return

</t>
<t tx="T132">def page_createList(self):

    #set_trace()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    log = owner.log

    problems = []
    addproblem = problems.append

    page.add(center(h3("Create New Mailing List"),
                    table(attr(cellspacing=0, cellpadding=3, align='center'),
                          tr(td(btnForm("Return to Main Page", showpage="main")),
                             td(btnForm("Return to Lists", showpage='manageLists')))),
                    br(),
                    ))

    # parse the HTTP headers to get the hostname used by the browser
    #print req.headers
    #print type(req.headers)
    #nl = re.compile("[\\r\\n]+")
    #headerlines = nl.split(req.headers)
    host = req.headers.getheader("Host").split(":")[0]
    log(3, "page_createList: you reached this web interface as http://%s:%s" % (host, config.httpPort))

    listName = fields['listName']
    sskPub = fields['sskPub']
    sskPriv = fields['sskPriv']
    genssk = fields['genssk']
    keySize = fields['keySize']
    keyAsTxt = fields['keyAsTxt']
    pop3Password = fields['pop3Password']
    pop3Password1 = fields['pop3Password1']
    insertHtl = fields['insertHtl']

    try:
        insertPeriod = int(fields['insertPeriod'])
    except:
        insertPeriod = 86400 # set the listsite DBR period to 24 hours
        
    cryptoKeyLen = 2048

    cmd = fields.get('cmd', 'new')

    # generate an ssk keypair if needed
    if genssk:
        node = self.owner.Node()
        sskPub, sskPriv = node.genkeypair()
        cmd = 'genssk'

    # processing of different stages is here in reverse order, to allow a 'drop-thru'
    # to previous state in case anything goes wrong. Data from earlier states gets
    # stored as form variables in later states

    if cmd == 'insertlist':

        # save in database, and do the insert

        # re-constitute key
        log(2, "re-constituting SSLCrypto key")
        key = SSLCrypto.key(b64dec(keyAsTxt))
        log(2, "SSLCrypto key reconstituted fine")

        # save all in database
        listAddr = owner.dbAddList(listName,
                                   cryptoKey=key,
                                   sskPub=sskPub,
                                   sskPriv=sskPriv,
                                   insertPeriod=insertPeriod,
                                   refreshInProgress=1,
                                   )

        log(2, "new list for %s added to database" % listName)

        # create node object
        try:
            insertHtl = int(insertHtl)
        except:
            addproblem("HTL must be a number from 0 to 40")
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            problems = []
            cmd = 'generatekey' # drop thru to prev state

        if not problems:
            node = self.owner.Node(htl=insertHtl)

            # stick the listsite in
            try:
                log(2, "inserting new list %s into freenet (entropy)" % listName)
                owner.fcpRefreshListsite(listAddr, htl=insertHtl)
                log(2, "insert of new list %s successful" % listName)
            except:
                addproblem("Failed to insert into Freenet/Entropy the info for your new mailing list:\n%s\n%s" % (
                    listAddr, exceptionString()))
                log(2, "New list insert problem:\n%s" % exceptionString())

            if problems:
                page.add(center(big(b("Sorry")),
                                "There were problems with your request:",
                                ulFromList(problems)))
                problems = []
                cmd = 'generatekey' # drop thru to prev state
            else:
                # everything went fine
                page.add(center(b("Success"), br(), br(),
                                "Your new freemail mailing list was successfully created and inserted).",
                                br(), br(),
                                "Your new FreeMail List address is:", br(),
                                b(listAddr), br(), br(),
                                "and it has been inserted into Freenet (or Entropy) as:", br(),
                                b(owner.db.lists[listAddr].listUriSite+"//"), br(), br(),

                                b("One last step:"), br(),
                                "Please now create a new mailing account in your normal "
                                "email program", br(),
                                small(i("(Mozilla, Evolution, Mutt, Pine, Kmail, Outlook Express etc)")), br(),
                                "and set it up with the following details:", br(), br(),

                                table(attr(align='center', cellspacing=0, cellpadding=3, border=1),
                                      tr(td(attr(align='center', colspan=2),
                                            b("General Settings:"))),
                                      tr(td(b("Name: ")),
                                         td("%s at FreeMail" % idName)),
                                      tr(td(b("Email Address: ")),
                                         td(attr(valign='top'), idAddr)),
                                      tr(td(attr(align='center', colspan=2),
                                            b("Receive Settings:"))),
                                      tr(td(attr(valign='top'),
                                            b("POP3 Server:")),
                                         td(attr(valign='top'), host)),
                                      tr(td(b("POP3 Port: ")),
                                         td(attr(valign='top'), config.popPort)),
                                      tr(td(b("POP3 Username: ")),
                                         td(attr(valign='top'), idAddr)),
                                      tr(td(b("POP3 Password: ")),
                                         td(attr(valign='top'), pop3Password)),
                                      tr(td(attr(align='center', colspan=2),
                                            b("Send Settings:"))),
                                      tr(td(b("SMTP Server: ")),
                                         td(attr(valign='top'), host)),
                                      tr(td(b("SMTP Port: ")),
                                         td(attr(valign='top'), config.smtpPort)),
                                      ),
                                ))

    if cmd == 'generatekey':

        # Generate a crypto key of required size, notify of insertion
        try:
            keySize = int(keySize)
        except:
            try:
                log(2,
                    "Strange problem - exception determining int(keySize)\n"
                    "keySize='%s'\n"
                    "defaulting to 3072-bit\n"
                    "%s" % (
                         keySize, exceptionString()))
            except:
                log(2,
                    "Even stranger - the local var 'keySize' has gone AWOL!!!\n"
                    "Defaulting to 3072-bit\n"
                    "%s" % exceptionString())
                keySize = 3072
        key = SSLCrypto.key(keySize, 'ElGamal')
        keyAsTxt = b64enc(key.exportKeyPrivate())

        page.add(center(form(attr(method="POST", action="/"),
                             input(type='hidden', name='showpage', value='createList'),
                             input(type='hidden', name='cmd', value='insertlist'),
                             input(type='hidden', name='listName', value=listName),
                             input(type='hidden', name='sskPub', value=sskPub),
                             input(type='hidden', name='sskPriv', value=sskPriv),
                             input(type='hidden', name='keyAsTxt', value=keyAsTxt),

                             b("Insert List into Freenet"), br(),
                             "Your new mailing list is ready to be inserted into Freenet (or Entropy)", br(),
                             "Please select a HTL (hops-to-live) parameter for this insertion.", br(),
                             small(i("(Note - the higher this value, the longer it will take now to "
                                     "complete the insertion - but the easier it will be for other "
                                     "FreeMail servers to locate your list.", br(),
                                     "Feel free to use a low value (even 0) if you are presently testing FreeMail.)",
                                     )), br(), br(),

                             table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                   tr(td(b("Insert HTL")),
                                      td(input(type='text', name='insertHtl', value="15"))),
                                   ),
                                   
                             btn("Insert into Freenet and Continue"), br(),
                             small(i("(Please click this only once)")),

                             )))

    if cmd == 'promptkeysize':

        # validate the SSK keypair
        node = owner.Node(htl=0)
        try:
            pub = node.put("test", "", "SSK@%s/xxxtest-%s" % (sskPriv, random.randint(0, 1000000))).uri.hash
            if pub != sskPub:
                addproblem("The public and private keys do not match")
        except:
            addproblem("Cannot contact node to validate your keypair")
            self.log(1, "Failed to contact node:\n%s" % exceptionString())

        if problems:
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            problems = []
            cmd = 'genssk' # drop thru to prev state
        else:
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createIdentity'),
                                 input(type='hidden', name='cmd', value='promptkeysize'),
                                 input(type='hidden', name='idName', value=idName),
                                 input(type='hidden', name='sskPub', value=sskPub),
                                 input(type='hidden', name='sskPriv', value=sskPriv),
                                 
                                 b("Secure your POP3 Mailbox"), br(), br(),
                                 "Please choose a password, that your mail program will use "
                                 "for retrieving your FreeMail messages via the POP3 port.", br(),br(),
                                 
                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(b("POP3 Password: ")),
                                          td(input(type="text", name="pop3Password", value=pop3Password, size=16))),
                                       tr(td(b("Confirm POP3 Password: ")),
                                          td(input(type="text", name="pop3Password1", value=pop3Password1, size=16))),
                                       ),
                                 btn("Continue"), br(),

                                 ),
                            ),
                     )
        
    if cmd == 'genssk':

        # validate chosen name
        lenListName = len(listName)
        if listName == '':
            addproblem("You did not enter a name")
        elif lenListName &lt; 3:
            addproblem("Name is too short")
        if lenListName &gt; 16:
            addproblem("Name is too long")
        if not re.match("^[a-zA-Z0-9\\-]+$", idName):
            addproblem("Name should only consist of letters, digits or hyphens")

        if problems:
            page.add(center(big(b("Sorry")),
                            "There were problems with your request:",
                            ulFromList(problems)))
            cmd = 'new' # drop thru to prev state

        else:
            # all ok - stick up ssk keys form
            page.add(center(form(attr(method="POST", action="/"),
                                 input(type='hidden', name='showpage', value='createList'),
                                 input(type='hidden', name='cmd', value='promptkeysize'),
                                 input(type='hidden', name='idName', value=listName),

                                 b("Secure your mailing list"), br(), br(),
                                 "In order to protect your mailing list against tampering "
                                 "attacks, we will need to secure it with a freenet SSK keypair.",
                                 br(),
                                 small(i("If you have an existing SSK keypair ",
                                         "(eg, one which you are using with an freesite), "
                                         "please enter it here.", br(),
                                         "Otherwise, please select 'Generate New Keys' to "
                                         "generate a whole new keypair.")), br(), br(),

                                 table(attr(cellspacing=0, cellpadding=3, align='center', border=0),
                                       tr(td(attr(colspan=2, align='center'),
                                             label(input(type='checkbox', name='genssk', value='yes'),
                                                   "Generate new SSK keys"
                                                   ))),
                                       tr(td(b("SSK Public Key: ")),
                                          td(input(type="text", name="sskPub", value=sskPub, size=40))),
                                       tr(td(b("SSK Private Key: ")),
                                          td(input(type="text", name="sskPriv", value=sskPriv, size=40))),
                                       tr(td(attr(colspan=2, align='center'),
                                             center(btn("Continue")))),
                                       )))),
                                       

    if cmd == 'new':
        # first stage - prompt for name
        page.add(center(form(attr(method="POST", action="/"),
                             input(type='hidden', name='showpage', value='createList'),
                             input(type='hidden', name='cmd', value='genssk'),

                             b("Please choose a short name"), br(),
                             small(i("(3-16 chars - letters, digits and '-' only)")),

                             table(attr(cellspacing=0, cellpadding=3, align='center', border=1),
                                   tr(td(input(type="text", name="listName", value=listName, size=18, maxlen=16)),
                                      ),
                                   ),
                             center(btn("Continue")),
                             ),
                        ),
                 )

    return

</t>
<t tx="T133">def page_manageInbound(self):

    #set_trace()

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config


    page.add(center(h3("Manage Inbound Messages"), br(),
                    "not implemented yet",
                    ))
    page.add(center(btnForm("Return to Main Page", showpage="main")))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T134">def page_manageOutbound(self):

    #set_trace()

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    page.add(center(h3("Manage Outbound Messages"), br(),
                    "not implemented yet",
                    ))
    page.add(center(btnForm("Return to Main Page", showpage="main")))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T135">def page_(self):

    #set_trace()

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    page.add(center(h3("Page")))
    page.add(btnForm("Return to Main Page", showpage="main"))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T136"></t>
<t tx="T137">def envDump(self):
    """
    Produces an HTTP environment dump for debugging purposes
    """
    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    t = table(attr(cellspacing=0, cellpadding=3, border=1),
              tr(td(attr(colspan=2, align='left'),
                    b("HTTP Environment"))),
              tr(td(b("Name")), td(b("Value"))))
    
    for k,v in os.environ.items():
        t.add(tr(td(k), td(v)))
        
    tf = table(attr(cellspacing=0, cellpadding=3, border=1),
               tr(td(attr(colspan=2, align='center'),
                     b("HTTP fields"))),
               tr(td(b("Name")), td(b("Value"))))
    for k in page.session.fields.keys():
        v = page.session.fields[k]
        tf.add(tr(td(k), td(v)))
    
    page.add(t)
    page.add(tf)
</t>
<t tx="T138">def isLoggedIn(self):

    """
    Returns 1 if user is logged in (has a non-stale cookie, or has given valid username/password
    """

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    # are we logged in?
    now = time.time()
    logged_in = 0
    if session.data.has_key('last_hit'):
        last_hit = float(session.data['last_hit'])
        elapsed = now - last_hit
        if elapsed &lt;= config.adminTimeout:
            session.data['last_hit'] = now
            logged_in = 1
        else:
            del session.data['last_hit']

    # no cookie - look for fields
    if session.env['REQUEST_METHOD'] == 'POST' \
           and fields.has_key('adminLogin') \
           and fields.has_key('adminPassword'):
        login = fields['adminLogin']
        password = fields['adminPassword']
        # print "trying to log in with login '%s', password '%s'" % (login, password)
        if (login == config.adminLogin) \
        and ((config.adminPassword == '') or hash(password) == config.adminPassword):
            session.data['last_hit'] = now
            logged_in = 1

    if logged_in and req.path.endswith('/logout'):
        logged_in = 0
        del session.data['last_hit']

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

    return logged_in
</t>
<t tx="T139">def showLoginPage(self):

    """
    Displays a login form
    """

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    page.add(center(h3("Freemail Administration Web Interface"),
                    ))

    f = table(attr(align='center', cellspacing=0, cellpadding=2, border=0),
              form(attr(method="POST", action="/"),
                   tr(td(attr(colspan=2, align='center'),
                         big(b("Please Log In")))),
                   tr(td(attr(align='right'),
                         b("Username:")),
                      td(attr(align='left'),
                         input(type='text', name='adminLogin', value=''))),
                   tr(td(attr(align='right'),
                         b("Password:")),
                      td(attr(align='left'),
                         input(type='password', name='adminPassword', value=''))),
                   tr(td(attr(colspan=2, align='center'),
                         btn("Login")),
                   )))
    f1 = table(attr(align='center', cellspacing=0, cellpadding=0, width='100%', height='50%'),
               tr(td(attr(align='center', valign='middle'),
                     f)))
    page.add(f1)

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T140"></t>
<t tx="T141">def wizard(self, state):

    method = getattr(self, "wizard_"+state)
    method()
</t>
<t tx="T142">def wizardSetState(self, state):
    """
    Changes the wizard's state to 'state', and displays
    the next pane.
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    # save state in database
    self.config.configState = state

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    
    # wipe out fields
    for fld in self.fields.keys():
        self.fields[fld] = ''
    
    # display next pane, or main if finished
    if state == 'ready':
        self.page_main()
    else:
        getattr(self, 'wizard_'+state)()
</t>
<t tx="T143">def wizardNextButton(self, label='Next'):
    return notag(btn(label.capitalize()),
                 input(type='hidden', name='pressednext', value=1))
</t>
<t tx="T144">def wizard_new(self):
    """
    Presents first pane of setup wizard
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    nextstate = 'security'

    if fields['pressednext']:
        self.wizardSetState(nextstate)

        self._dbUnlock()
        # -^-^-^-^-^-^- UNLOCK DATABASE --------------
        return
                            
    page.add(center(h1("Welcome to Freemail"),
                    "This wizard will walk you through the process of "
                    "configuring your Freemail installation.",
                    br(), br(),
                    "Please click 'Next' to continue", br(), br(),
                    )
             )
    
    page.add(center(form(attr(method='post', action='/'),
                         self.wizardNextButton(),
                         )
                    )
             )

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T145">def wizard_security(self):
    """
    Prompts for username/password
    """
    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    username = fields['username'] or config.adminLogin
    password = fields['password']
    password1 = fields['password1']

    nextstate = 'fcp'

    errors = ''

    if fields['pressednext']:
        # validate username/password
        problems = []
        if username == '':
            problems.append("You must enter a username")
        if password == '':
            problems.append("You must enter a password")
        elif password != password1:
            problems.append("Your passwords do not match")
        elif len(password) &lt; 6:
            problems.append("Your password is too short")
        
        if problems:
            errors = notag("Sorry, but there were errors:", ulFromList(problems))
        else:
            # write new values to database
            config.adminLogin = username
            config.adminPassword = hash(password)
            owner.dbSave()

            # and move on
            self.wizardSetState(nextstate)

            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return

    page.add(center(h2("Freemail Setup: Web Interface Security"),
                    errors))

    # display entry form
    page.add(center(form(attr(action="/", method="POST"),
                         table(attr(cellspacing=0, cellpadding=5),
                               tr(td(attr(colspan=2, align='center'),
                                     h3("Please choose a username and password"),
                                     "(For logging into this web interface once you're set up)",
                                     )),
                               tr(td(attr(align='right'),
                                     b("Username: ")),
                                  td(input(type='text', name='username', size=12, value=username))),
                               tr(td(attr(align='right'),
                                     b("Password: ")),
                                  td(input(type='password', name='password', size=12, value=password))),
                               tr(td(attr(align='right'),
                                     b("Confirm Password: ")),
                                  td(input(type='password', name='password1', size=12, value=password1))),
                               tr(td(attr(align='center', colspan=2),
                                     small(i("(password must be at least 6 characters)")))),
                               ),
                         self.wizardNextButton(),
                         )))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T146">def wizard_fcp(self):
    """
    Configures the node FCP settings
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config
    log = owner.log

    errors = ''

    nextstate = 'servers'

    #print "fields: ", repr(fields)
    fcpHost = fields['fcpHost'] or config.fcpHost
    #print "fcpPort = '%s'" % fields['fcpPort']

    fcpPort = int(config.fcpPort)

    if fields['pressednext']: # user has submitted form

        # validate FCP host/port
        problems = []

        fcpPort = fields['fcpPort']
        try:
            fcpPort = int(fcpPort)
        except:
            pass
        #print "fcpPort = '%s'" % fcpPort

        # validate fields
        if fcpHost == '':
            problems.append("You must specify an FCP host")
        if not fcpPort:
            problems.append("You must specify an FCP port")
        elif type(fcpPort) is not type(0):
            problems.append("FCP Port must be a number")
        elif fcpPort &lt; 1 or fcpPort &gt; 65535:
            problems.append("Invalid FCP Port '%s' - should be between 1 and 65535" % fcpPort)

        #print problems

        # try a handshake if ok so far
        if not problems:
            try:
                n = freenet.node(fcpHost, fcpPort)
                n._handshake()
            except freenet.FreenetFcpConnectError:
                problems.append("Can't connect to '%s:%s'" % (fcpHost, fcpPort))
            except freenet.FreenetFcpError:
                problems.append("Connected to '%s:%s', but it's not talking FCP" % (fcpHost, fcpPort))
            except:
                log(2, "Exception talking to FCP port at %s:%s\n%s" % (fcpHost, fcpPort, exceptionString()))
                problems.append("Some weird problem talking to FCP port at '%s:%s'" % (fcpHost, fcpPort))

        if problems:
            errors = notag("Sorry, but there were errors:", ulFromList(problems))
        else:
            # write new values to database
            config.fcpHost = fcpHost
            config.fcpPort = fcpPort
            owner.dbSave()

            # and move on
            self.wizardSetState(nextstate)

            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return

    page.add(center(h2("Freemail Setup: Freenet/Entropy Node Interface Settings"),
                    errors))

    # display entry form
    page.add(center(form(attr(action="/", method="POST"),
                         table(attr(cellspacing=0, cellpadding=5),
                               tr(td(attr(colspan=2, align='center'),
                                     h3("Please choose a Freenet or Entropy FCP interface"))),
                               tr(td(attr(align='right'),
                                     b("FCP Hostname (or IP): ")),
                                  td(input(type='text', name='fcpHost', size=20, value=fcpHost))),
                               tr(td(attr(align='right'),
                                     b("FCP Port: ")),
                                  td(input(type='text', name='fcpPort', size=12, value=fcpPort))),
                               tr(td(attr(align='center', colspan=2),
                                     b("Note - we will test this interface, so please ensure "
                                       "you have a node running"))),
                               ),
                         input(type='hidden', name='test', value='1'),
                         self.wizardNextButton(),
                         )))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T147">def wizard_servers(self):
    """
    Enter listening ports for servers
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config


    errors = ''

    nextstate = 'misc'

    httpPort = config.httpPort
    popPort = config.popPort
    smtpPort = config.smtpPort
    telnetPort = config.telnetPort

    httpHosts = owner.db.httpHosts
    popHosts = owner.db.popHosts
    smtpHosts = owner.db.smtpHosts
    telnetHosts = owner.db.telnetHosts


    if fields['pressednext']: # user has submitted form

        # validate server ports
        problems = []

        httpPort = fields['httpPort']
        try:
            httpPort = int(httpPort)
        except:
            pass
        popPort = fields['popPort']
        try:
            popPort = int(popPort)
        except:
            pass
        smtpPort = fields['smtpPort']
        try:
            smtpPort = int(smtpPort)
        except:
            pass
        telnetPort = fields['telnetPort']
        try:
            telnetPort = int(telnetPort)
        except:
            pass

        httpHosts = [h.strip() for h in fields['httpHosts'].split(",")]
        smtpHosts = [h.strip() for h in fields['smtpHosts'].split(",")]
        popHosts = [h.strip() for h in fields['popHosts'].split(",")]
        telnetHosts = [h.strip() for h in fields['telnetHosts'].split(",")]

        # validate fields
        if not httpPort:
            problems.append("You must specify an HTTP port")
        elif type(httpPort) is not type(0):
            problems.append("HTTP Port must be a number")
        elif httpPort &lt; 1 or httpPort &gt; 65535:
            problems.append("Invalid HTTP Port '%s' - should be between 1 and 65535" % httpPort)

        if not popPort:
            problems.append("You must specify a POP3 port")
        elif type(popPort) is not type(0):
            problems.append("POP3 Port must be a number")
        elif popPort &lt; 1 or popPort &gt; 65535:
            problems.append("Invalid POP3 Port '%s' - should be between 1 and 65535" % popPort)

        if not smtpPort:
            problems.append("You must specify an SMTP port")
        elif type(smtpPort) is not type(0):
            problems.append("SMTP Port must be a number")
        elif smtpPort &lt; 1 or smtpPort &gt; 65535:
            problems.append("Invalid SMTP Port '%s' - should be between 1 and 65535" % smtpPort)

        if not telnetPort:
            problems.append("You must specify a Telnet port")
        elif type(telnetPort) is not type(0):
            problems.append("Telnet Port must be a number")
        elif telnetPort &lt; 1 or telnetPort &gt; 65535:
            problems.append("Invalid Telnet Port '%s' - should be between 1 and 65535" % telnetPort)

        if problems:
            errors = notag("Sorry, but there were errors:", ulFromList(problems))
        else:
            # write new values to database
            config.httpPort = httpPort
            config.popPort = popPort
            config.smtpPort = smtpPort
            config.telnetPort = telnetPort

            # now the allowed hosts
            db.httpHosts = httpHosts
            db.smtpHosts = smtpHosts
            db.popHosts = popHosts
            db.telnetHosts = telnetHosts

            owner.dbSave()

            # launch the mailservers
            owner.startMailServers()
            owner.startTelnetServer()

            # and move on
            self.wizardSetState(nextstate)

            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return

    page.add(center(h2("Freemail Setup: User Interface Port Settings"),
                    errors))

    # display entry form
    page.add(center(form(attr(action="/", method="POST"),
                         table(attr(cellspacing=0, cellpadding=5, border=0, width='70%'),
                               tr(td(attr(colspan=3, align='center'),
                                     h3("Please choose ports for the Freemail user interface servers")),
                                  ),

                               tr(td(attr(align='left'),
                                     b("HTTP Port: "), br(),
                                     ),
                                  td(input(type='text', name='httpPort', size=12, value=httpPort)),
                                  td(small(i("(For setting up and managing Freemail "
                                             "via your web browser, "
                                             "just like you're doing now)")),
                                     )
                                  ),
                               tr(td(b("Allowed HTTP hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="httpHosts", size=32, maxlen=80, value=",".join(httpHosts))),
                                     td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                                "web (HTTP) interface are accepted.)"))),
                                  ),
                               tr(td(attr(colspan=3), hr())),

                               tr(td(attr(align='left'),
                                     b("POP3 Port: "), br(),
                                     ),
                                  td(input(type='text', name='popPort', size=12, value=popPort)),
                                  td(small(i("(for receiving incoming messages "
                                             "from Freemail into your email client)")),
                                     )
                                  ),
                               tr(td(b("Allowed POP3 hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="popHosts", size=32, maxlen=80, value=",".join(popHosts))),
                                  td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                             "inbound mail (POP3) interface are accepted.)"))),
                                  ),
                               tr(td(attr(colspan=3), hr())),

                               tr(td(attr(align='left'),
                                     b("SMTP Port: "), br(),
                                     ),
                                  td(input(type='text', name='smtpPort', size=12, value=smtpPort)),
                                  td(small(i("(for sending messages via Freemail "
                                             "from your email client)")),
                                     )
                                  ),
                               tr(td(b("Allowed SMTP hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="smtpHosts", size=32, maxlen=80, value=",".join(smtpHosts))),
                                     td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                             "outbound mail (SMTP) interface are accepted.)"))),
                                  ),
                               tr(td(attr(colspan=3), hr())),

                               tr(td(attr(align='left'),
                                     b("Telnet Port: "), br(),
                                     ),
                                  td(input(type='text', name='telnetPort', size=12, value=telnetPort)),
                                  td(small(i("(Provides a telnet interface to the pythonic console)")),
                                     )
                                  ),
                               tr(td(b("Allowed Telnet hosts"), br(),
                                     small(i("(comma-separated)")),
                                     ),
                                  td(input(type="text", name="telnetHosts", size=32, maxlen=80, value=",".join(telnetHosts))),
                                  td(small(i("(List of hostnames or IP addresses from which connections to the FreeMail "
                                             "telnet console interface are accepted.)"))),
                                  ),
                               tr(td(attr(colspan=3), hr())),

                               tr(td(attr(align='left', colspan=3),
                                     b("Notes:"),
                                     ul(li("Port values less than 1025 will not work, "
                                           "unless you're running Freemail as root"),
                                        li("Changes to the HTTP port won't take effect "
                                           "until you restart Freemail"),
                                        li("If you fill in valid values for all ports, "
                                           "the Freemail POP3, SMTP and Telnet servers will "
                                           "start up when you click 'Next'"),
                                        ),
                                     )),
                               ),
                         self.wizardNextButton(),
                         )))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------</t>
<t tx="T148">def wizard_misc(self):
    """
    Enter miscellaneous config info
    """
    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    errors = ''

    nextstate = 'finish'

    adminTimeout = config.adminTimeout
    htlSend = config.htlSend
    htlReceive = config.htlReceive

    receiptAll = config.receiptAll
    if receiptAll:
        tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes" CHECKED&gt;'
    else:
        tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes"&gt;'

    storeDir = config.storeDir
    rxMaxRetries = config.rxMaxRetries
    txMaxRetries = config.txMaxRetries
    cryptoKeySize = config.cryptoKeySize
    slotLookAhead = config.slotLookAhead
    txBackoffInit = config.txBackoffInit
    txBackoffMult = config.txBackoffMult

    if fields['pressednext']: # user has submitted form

        # validate server ports
        problems = []

        adminTimeout = fields['adminTimeout']
        try:
            adminTimeout = int(adminTimeout)
        except:
            pass

        htlSend = fields['htlSend']
        try:
            htlSend = int(htlSend)
        except:
            pass
        htlReceive = fields['htlReceive']
        try:
            htlReceive = int(htlReceive)
        except:
            pass

        receiptAll = (fields['receiptAll'] == 'yes')
        if receiptAll:
            tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes" CHECKED&gt;'
        else:
            tagReceiptAll = '&lt;input type="checkbox" name="receiptAll" value="yes"&gt;'
        
        storeDir = fields['storeDir']

        rxMaxRetries = fields['rxMaxRetries']
        try:
            rxMaxRetries = int(rxMaxRetries)
        except:
            pass

        txMaxRetries = fields['txMaxRetries']
        try:
            txMaxRetries = int(txMaxRetries)
        except:
            pass

        cryptoKeySize = fields['cryptoKeySize']
        try:
            cryptoKeySize = int(cryptoKeySize)
        except:
            pass

        maxWorkerThreads = fields['maxWorkerThreads']
        try:
            maxWorkerThreads = int(maxWorkerThreads)
        except:
            pass

        slotLookAhead = fields['slotLookAhead']
        try:
            slotLookAhead = int(slotLookAhead)
        except:
            pass
        txBackoffInit = fields['txBackoffInit']
        try:
            txBackoffInit = int(txBackoffInit)
        except:
            pass
        txBackoffMult = fields['txBackoffMult']
        try:
            txBackoffMult = float(txBackoffMult)
        except:
            pass

        # validate fields

        if not adminTimeout:
            problems.append("You must specify an admin timeout")
        elif type(adminTimeout) is not type(0):
            problems.append("Admin timeout must be a number")
        elif adminTimeout &lt; 60:
            problems.append("Dumb value for admin timeout - "
                            "please try at least 60 (recommend 3600)")

        if htlSend != 0 and not htlSend:
            problems.append("You must specify an HTL value for sending")
        elif type(htlSend) is not type(0):
            problems.append("HTL TX value must be a number")
        elif htlSend  &gt; 100:
            problems.append("Dumb HTL TX value '%s' - try something like 20" % htlSend)

        if htlReceive != 0 and not htlReceive:
            problems.append("You must specify an HTL value for receiving")
        elif type(htlReceive) is not type(0):
            problems.append("HTL RX value must be a number")
        elif htlReceive  &gt; 100:
            problems.append("Dumb HTL RX value '%s' - try something like 20" % htlReceive)

        if not storeDir:
            problems.append("You must choose a datastore directory")
        else:
            if os.path.isdir(storeDir):
                # try to set permissions
                try:
                    os.chmod(storeDir, 0700)
                except:
                    problems("Failed to set permissions for store directory '%s' "
                             "please choose another path" % storeDir)
            else:
                # try to create store directory now
                try:
                    os.makedirs(storeDir, 0700)
                except:
                    problems.append("Failed to create store directory '%s' "
                                    "please try something else" % storeDir)

        if not rxMaxRetries:
            problems.append("You must specify a maximum receive retry count")
        elif type(rxMaxRetries) is not type(0):
            problems.append("receive retry count should be a number")
        elif rxMaxRetries not in range(1, 20):
            problems.append("Invalid receive retry limit '%s' - "
                            "please choose something between 1 and 20" % rxMaxRetries)

        if not txMaxRetries:
            problems.append("You must specify a maximum transmit retry count")
        elif type(txMaxRetries) is not type(0):
            problems.append("transmit retry count should be a number")
        elif txMaxRetries not in range(1, 20):
            problems.append("Invalid transmit retry limit '%s' - "
                            "please choose something between 1 and 20" % txMaxRetries)

        if not cryptoKeySize:
            problems.append("You must specify an encryption key size")
        elif type(cryptoKeySize) is not type(1):
            problems.append("Encryption key size should be a number")
        elif cryptoKeySize not in [1024, 2048, 3072, 4096]:
            problems.append("Crazy key size '%s' - "
                            "please choose 1024,2048,3072 or 4096 - (recommend 2048 or higher)" % cryptoKeySize)

        if not slotLookAhead:
            problems.append("You must specify an initial rx slot look-ahead count")
        elif type(slotLookAhead) is not type(1):
            problems.append("RX Slot look-ahead should be a number, best 2-10")
        elif slotLookAhead &lt; 1 or slotLookAhead &gt; 10:
            problems.append("Crazy RX slot look-ahead '%s' - "
                            "please choose something between 1 and 10 (recommend 5)" % slotLookAhead)

        if not txBackoffInit:
            problems.append("You must specify an initial send backoff interval")
        elif type(txBackoffInit) is not type(1):
            problems.append("Initial send backoff interval should be a number, best 7200 or more")
        elif txBackoffInit &lt; 300 or txBackoffInit &gt; 86400:
            problems.append("Crazy initial send backoff interval '%s' - "
                            "please choose something between 1800 and 86400" % txBackoffInit)

        if not txBackoffMult:
            problems.append("You must specify an rx slot backoff multiplier")
        elif type(txBackoffMult) not in [type(1), type(1.1)]:
            problems.append("Send backoff multiplier should be a number from 1.1 to 4.0")
        elif txBackoffMult &lt; 1.1 or txBackoffMult &gt; 4.0:
            problems.append("Crazy send backoff multiplier '%s' - "
                            "please choose something between 1.1 and 4.0" % txBackoffMult)

        if problems:
            errors = notag("Sorry, but there were errors:", ulFromList(problems))
        else:
            # write new values to database
            config.adminTimeout = adminTimeout
            config.htlSend = htlSend
            config.htlReceive = htlReceive
            config.receiptAll = receiptAll
            config.storeDir = storeDir
            config.rxMaxRetries = rxMaxRetries
            config.txMaxRetries = txMaxRetries
            config.cryptoKeySize = cryptoKeySize
            config.slotLookAhead = slotLookAhead
            config.txBackoffInit = txBackoffInit
            config.txBackoffMult = txBackoffMult
            owner.dbSave()

            # and move on
            self.wizardSetState(nextstate)

            self._dbUnlock()
            # -^-^-^-^-^-^- UNLOCK DATABASE --------------
            return

    page.add(center(h2("Freemail Setup: Miscellaneous Settings"),
                    errors))

    page.add(center(small(i("(If there are any of these settings you don't understand, "
                            "just go with the default values)"))))
    # display entry form
    page.add(center(form(attr(action="/", method="POST"),
                         table(attr(cellspacing=0, cellpadding=5),
                               tr(td(attr(colspan=3, align='center'),
                                     h3("Please now review some miscellaneous settings"))),

                               tr(td(attr(align='right'),
                                     b("Admin Timeout (seconds): "),
                                     ),
                                  td(input(type='text', name='adminTimeout', size=20, value=adminTimeout)),
                                  td(small(i("(Security - if you exceed this time between "
                                             "accesses to the web interface, you'll have to "
                                             "log in again.)"
                                             )),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("HTL for Sending: "),
                                     ),
                                  td(input(type='text', name='htlSend', size=3, value=htlSend)),
                                  td(small(i("(hops-to-live - specifies how deeply the "
                                             "messages you send penetrate the network. If too low, "
                                             "people won't be able to receive your messages. "
                                             "If too high, your Freemail node will run too slow. "
                                             "I'd recommend something between 15 and 40.")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("HTL for Receiving: "),
                                     ),
                                  td(input(type='text', name='htlReceive', size=3, value=htlReceive)),
                                  td(small(i("(hops-to-live - specifies how deeply Freemail "
                                             "probes within the network to extract messages "
                                             "sent to you. "
                                             "If too low, you won't receive messages sent to you. "
                                             "If too high, your Freemail node will run too slow. "
                                             "I'd recommend something between 15 and 40.")),
                                     )
                                  ),

                               tr(td(b("Issue receipts for all sent messages?")),
                                  td(tagReceiptAll),
                                  td(small(i("(Normally, when messages are successfully delivered, "
                                             "you won't receive anything - only if a message is not "
                                             "delivered will a message be written to the sending "
                                             "identity's POP mailbox. But if you enable this option, "
                                             "the sending identity will receive a yes/no confirmation "
                                             "for all outbound messages, regardless of outcome.")),
                                     ),
                                  ),

                               tr(td(attr(align='right'),
                                     b("Store Directory: "),
                                     ),
                                  td(input(type='text', name='storeDir', size=20, value=storeDir)),
                                  td(small(i("(Relative or absolute directory path for "
                                             "storing your inbound and outbound message bodies. "
                                             "If this path doesn't exist, this wizard will try to "
                                             "create it and set its permissions.)")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("Receive Retry Count: "),
                                     ),
                                  td(input(type='text', name='rxMaxRetries', size=6, value=rxMaxRetries)),
                                  td(small(i("Number of times to retry retrievals. Recommend around 2-8.)")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("Transmission Retry Count: "),
                                     ),
                                  td(input(type='text', name='txMaxRetries', size=6, value=txMaxRetries)),
                                  td(small(i("Number of times to retry sends. Recommend around 2-8.)")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("Encryption Key Size: "),
                                     ),
                                  td('',
                                     #input(type='text', name='cryptoKeySize', size=6, value=cryptoKeySize),
                                     '&lt;select name="cryptoKeySize"&gt;'
                                     '&lt;option label="1024 bits (low grade)" value="1024"&gt;1024 bits (low grade)&lt;/option&gt;'
                                     '&lt;option label="2048 bits (personal grade)", value="2048" selected&gt;2048 bits (personal grade)&lt;/option&gt;'
                                     '&lt;option label="3072 bits (commercial grade)", value="3072"&gt;3072 bits (commercial grade)&lt;/option&gt;'
                                     '&lt;option label="4096 bits (military grade)", value="4096"&gt;4096 bits (military grade)&lt;/option&gt;'
                                     "&lt;/select&gt;",
                                     ),
                                  td(small(i("Size of encryption key for receiving messages. Recommend 2048 or above.)")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("Receive Slot Look-Ahead: "),
                                     ),
                                  td(input(type='text', name='slotLookAhead', size=5, value=slotLookAhead)),
                                  td(small(i("(used for retrieving incoming session and mail messages. "
                                             "This specifies how many receive slots to 'look ahead' "
                                             "when polling for incoming messages. "
                                             "Valid values are between 1 and 10 (suggest 5)")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("Initial Send Retry Backoff Interval: "), br(),
                                     small(i("(seconds)"))
                                     ),
                                  td(input(type='text', name='txBackoffInit', size=5, value=txBackoffInit)),
                                  td(small(i("(used for retrying message sends. "
                                             "This is the &lt;b&gt;initial&lt;/b&gt; time in seconds to wait between send retries. "
                                             "Valid values are between 1800 (30 mins) and 86400 (1 day))")),
                                     )
                                  ),

                               tr(td(attr(align='right'),
                                     b("Send Retry Backoff Multiplier: "), br(),
                                     small(i("(float)"))
                                     ),
                                  td(input(type='text', name='txBackoffMult', size=5, value=txBackoffMult)),
                                  td(small(i("(used for exponential backoff/retry of message re-sends. "
                                             "This value gets multiplied by the backoff interval each time a message "
                                             "send fails. "
                                             "Eg, if 1.5, and init value is 3600, then Freemail will retry the send after "
                                             "1 hour, then 1.5 hours, then 2.25 hours, 3.375 hours and so on."
                                             "Valid values are between 1.1 and 4.0, recommend 2.0)")),
                                     )
                                  ),

                               tr(td(attr(colspan=3, align='center'),
                                     self.wizardNextButton())),
                               )
                         )))

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

</t>
<t tx="T149">def wizard_finish(self):
    """
    Completion of setup
    """

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    owner = self.owner
    db = owner.db
    config = db.config

    errors = ''

    nextstate = 'ready'

    page.add(center(h2("Freemail Setup: Completion")))

    page.add(center(h3("Congratulations!"),
                    p("Your Freemail setup is now complete."),
                    p("Freemail is now talking to a live Freenet (or Entropy) node, "
                      "and has launched its POP3 and SMTP mail servers."),
                    p("All you need to do now is log in, and create one or more "
                      "mailing 'identities' - aliases under which you send messages "
                      "to people. When you log in, just click on &lt;b&gt;Manage Identities&lt;/b&gt;"),
                    p("Once you have an identity set up, you'll be able to start "
                      "sending and receiving emails with an unprecedented level of privacy."),

                    form(attr(action="/", method="POST"),
                         table(attr(align='center', cellspacing=0, cellpadding=4, border=0),
                               tr(td(self.wizardNextButton())))),
                    ))

    # no validation needed here
    config.configState = nextstate

    self.owner.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
</t>
<t tx="T150">class HTTPServer(SocketServer.ThreadingMixIn, BaseHTTPServer.HTTPServer):
#class HTTPServer(BaseHTTPServer.HTTPServer):

    @others
</t>
<t tx="T151">request_queue_size = 256
</t>
<t tx="T152">def __init__(self, **kw):
    """
    Creates a HTTP server.
    
    No arguments.
    
    Keywords:
     - owner - an object passed in by whoever owns this server - compulsory;
       note that this is available as the 'owner' attribute within
       request handler objects.
     - WebUI - a callable that handles incoming hits.
     - log - a function for logging which accepts 2 arguments:
         - a log level - 1=critical, 2=important, 3=trivial, 4=debug
     - port - default 8000
     - greeting - a text line to print to console when server fires up
     - handlerClass - a class constructor for a POP Request Handler
     - allowedHosts - list of allowed hosts
    """

    allowedHosts = kw.get('allowedHosts', None)
    if allowedHosts:
        bindAddr = ''
    else:
        bindAddr = '127.0.0.1'
        allowedHosts = ['127.0.0.1']
    port = kw.get('port', 8000)

    self.name = kw.get('name', "David's hacked-up HTTP server")
    self.log = kw['log']
    self.webUI = kw['webUI']
    self.owner = kw['owner']
    self.bindAddr = bindAddr
    self.port = port
    self.handlerClass = kw.get('handlerClass', HTTPRequestHandler)
    self.allowedHosts = allowedHosts

    self.request_queue_size = 256
    self.daemon_thread = False

    #set_trace()

    BaseHTTPServer.HTTPServer.__init__(self, (bindAddr, port), self.handlerClass)
</t>
<t tx="T153">def run(self):

    print "** "+self.name+" now listening on port %s" % self.port
    self.serve_forever()


</t>
<t tx="T154">class HTTPRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    @others

</t>
<t tx="T155"></t>
<t tx="T156">def __init__(self, request, client_address, server):

    self.server_version = "FreemailHTTP/" + version

    self.extensions_map = mimetypes.types_map.copy()
    self.extensions_map.update({
        '': 'application/octet-stream', # Default
        '.py': 'text/plain',
        '.c': 'text/plain',
        '.h': 'text/plain',
        })
    
    # Determine platform specifics
    self.have_fork = hasattr(os, 'fork')
    self.have_popen2 = hasattr(os, 'popen2')
    self.have_popen3 = hasattr(os, 'popen3')
    
    # Make rfile unbuffered -- we need to read one line and then pass
    # the rest to a subprocess, so we can't use buffered input.
    self.rbufsize = 0
    
    self.cgi_directories = ['/cgi-bin', '/htbin']
    
    self.nobody = None


   
    self.server = server
    self.client_address = client_address
    self.request = request
    self.owner = server.owner
    self.log = server.log
    
    BaseHTTPServer.BaseHTTPRequestHandler.__init__(self, request, client_address, server)</t>
<t tx="T157">def do_GET(self):
    """Serve a GET request."""
    self.run_cgi()
</t>
<t tx="T158">def do_POST(self):
    """Serve a POST request.

    This is only implemented for CGI scripts.

    """
    self.run_cgi()
</t>
<t tx="T159">def do_HEAD(self):
    """Serve a HEAD request."""
    self.run_cgi()
</t>
<t tx="T160">def run_cgi(self):
    """Execute a CGI script."""

    splitpath = os.path.split(self.path)
    #print "self.path='%s'" % self.path
    #print "splitted='%s'" % str(splitpath)
    dir, rest = splitpath

    i = rest.rfind('?')
    if i &gt;= 0:
        rest, query = rest[:i], rest[i+1:]
    else:
        query = ''

    #print "rest='%s' query='%s'" % (rest, query)

    i = rest.find('/')
    if i &gt;= 0:
        script, rest = rest[:i], rest[i:]
    else:
        script, rest = rest, ''
    #self.log(4, "self.path='%s'" % self.path)
    scriptname = dir + '/' + script
    #self.log(4, "scriptname='%s'" % scriptname)

    # I don't think we need this
    #scriptfile = self.translate_path(scriptname)
    #self.log(4, "scriptfile='%s'" % scriptfile)

    #self.log(4, "ATTEMPT: '%s" % self.translate_path(self.path))
    #self.log(4, "ATT: '%s" % urllib.unquote(self.path))

    if scriptname.startswith("//"):
        scriptname = scriptname[1:]
    #self.log(4, "scriptname='%s'" % scriptname)

    scriptname = urllib.unquote(self.path).split("?", 1)[0]

    # Reference: http://hoohoo.ncsa.uiuc.edu/cgi/env.html
    # XXX Much of the following could be prepared ahead of time!
    env = {}
    env['SERVER_SOFTWARE'] = self.version_string()
    env['SERVER_NAME'] = self.server.server_name
    env['GATEWAY_INTERFACE'] = 'CGI/1.1'
    env['SERVER_PROTOCOL'] = self.protocol_version
    env['SERVER_PORT'] = str(self.server.server_port)
    env['REQUEST_METHOD'] = self.command
    uqrest = urllib.unquote(rest)
    env['PATH_INFO'] = uqrest
    env['PATH_TRANSLATED'] = self.translate_path(uqrest)
    env['SCRIPT_NAME'] = scriptname
    if query:
        #print "query='%s'" % query
        env['QUERY_STRING'] = query
    host = self.address_string()
    if host != self.client_address[0]:
        env['REMOTE_HOST'] = host
    env['REMOTE_ADDR'] = self.client_address[0]
    # XXX AUTH_TYPE
    # XXX REMOTE_USER
    # XXX REMOTE_IDENT
    if self.headers.typeheader is None:
        env['CONTENT_TYPE'] = self.headers.type
    else:
        env['CONTENT_TYPE'] = self.headers.typeheader
    length = self.headers.getheader('content-length')
    if length:
        env['CONTENT_LENGTH'] = length
    accept = []
    for line in self.headers.getallmatchingheaders('accept'):
        if line[:1] in "\t\n\r ":
            accept.append(line.strip())
        else:
            accept = accept + line[7:].split(',')
    env['HTTP_ACCEPT'] = ','.join(accept)
    ua = self.headers.getheader('user-agent')
    if ua:
        env['HTTP_USER_AGENT'] = ua
    co = filter(None, self.headers.getheaders('cookie'))
    if co:
        env['HTTP_COOKIE'] = ', '.join(co)
    # XXX Other HTTP_* headers
    if not self.have_fork:
        # Since we're setting the env in the parent, provide empty
        # values to override previously set values
        for k in ('QUERY_STRING', 'REMOTE_HOST', 'CONTENT_LENGTH',
                  'HTTP_USER_AGENT', 'HTTP_COOKIE'):
            env.setdefault(k, "")

    # swap to our environment and launch script
    #oldenviron = os.environ
    #os.environ = env
    os.environ.update(env)

    # prevent unauthorised remote access
    if env['REMOTE_ADDR'] not in self.server.allowedHosts:
        self.log(1, "run_cgi: Rejecting REMOTE_ADDR '%s'" % env['REMOTE_ADDR'])
        self.send_error(403, "You cannot access this site from your host")
        return

    self.send_response(200, "Script output follows")
    try:
        # pass to the http ui generator
        self.server.webUI(self)
    except:
        self.server.handle_error(self.request, self.client_address)
    #os.environ = oldenviron

</t>
<t tx="T161">def translate_path(self, path):
    """Translate a /-separated PATH to the local filename syntax.

    Components that mean special things to the local file system
    (e.g. drive or directory names) are ignored.  (XXX They should
    probably be diagnosed.)

    """
    path = posixpath.normpath(urllib.unquote(path))
    words = path.split('/')
    words = filter(None, words)
    path = os.getcwd()
    for word in words:
        drive, word = os.path.splitdrive(word)
        head, word = os.path.split(word)
        if word in (os.curdir, os.pardir): continue
        path = os.path.join(path, word)
    return path

</t>
<t tx="T162">def log_message(self, format, *args):
    """
    
    """
    self.log(3, format%args)

</t>
<t tx="T163"></t>
<t tx="T164">class POPserver:
    """
    Don't run more than one instance of this in a single process.
    """
    @others
</t>
<t tx="T165">def __init__(self, **kw):
    """
    Creates a POP server.
    
    No arguments.
    
    Keywords:
     - freemail - a freemailServer object - compulsory
     - bindaddr - TCP bind address, default '127.0.0.1'
     - port - default 110
     - handlerClass - a class constructor for a POP Request Handler
    """
    self.freemail = kw['freemail']
    self.bindaddr = kw.get('bindaddr', '')
    self.port = kw.get('port', 110)
    self.handlerClass = kw['handlerClass']

</t>
<t tx="T166">def exitNormal(self, signum, frame):
    sys.exit(0)
</t>
<t tx="T167">def run(self):

    #try:
    #    signal.signal(signal.SIGTERM, self.exitNormal)
    #    signal.signal(signal.SIGABRT, self.exitNormal)
    #except:
    #    print "Can't hook signals on this platform"

    while 1:
        try:
            self.server = SocketServer.ThreadingTCPServer(
                (self.bindaddr, self.port),
                self.handlerClass,
                )
            break
        except:
            print "Can't bind to POP3 socket %s - waiting to retry..." % self.port
            time.sleep(10)

    self.server.socket.setsockopt(
        socket.SOL_SOCKET,
        socket.SO_REUSEADDR,
        1)

    self.server.freemail = self.freemail

    print "** Freemail POP3 server now listening on port %d" % self.port

    while 1:
        self.server.handle_request()

</t>
<t tx="T168">class POPRequestHandler(SocketServer.StreamRequestHandler):
    @others
</t>
<t tx="T169">def __init__(self, sock, addr, server, **kw):
    
    #self.freemail = kw['freemail']
    #del kw['freemail']
    self.freemail = server.freemail
    self.log = self.freemail.log
    self.remotePeer = addr[0]

    #print "POPRequestHandler: args='%s' kw='%s'" % (str(args), str(kw))

    self.log(3, "POPRequestHandler: addr=%s" % str(addr))
    SocketServer.StreamRequestHandler.__init__(self, sock, addr, server, **kw)



</t>
<t tx="T170">COMMANDS = ("QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP",
            "RSET", "TOP", "UIDL", "USER", "PASS")
</t>
<t tx="T171">def handle(self):

    if self.remotePeer not in self.freemail.db.popHosts:
        self.server.close_request(self.request)
        return

    wf = self.request.makefile('wb')
    session = POPSession(wf, self.freemail)
    session.write_ok("POP3 server ready")
    wf.flush()

    #print "POPRequestHandler: self='%s'" % str(self)
    #print "POPRequestHandler: freemail='%s'" % str(self.freemail)

    while 1:
        try:
            line = self.request.recv(2048)
            if line == '':
                wf.close()
                self.server.close_request(self.request)
                return

            self.freemail.log(5, "POPRequestHandler: line='%s'" % line.strip())
            comm = line.split()
            if not comm:
                continue
            (cmd, args) = (comm[0].upper(), comm[1:])
            if cmd not in self.COMMANDS:
                raise Error, "Unknown command"
            method = getattr(session, cmd, None)
            if method is None:
                raise Error, "Unsupported command"
            method(*args)
        except (TypeError, Error), msg:
            wf.write("-ERR %s\r\n" % str(msg))
            if not isinstance(msg, Error):
                self.log(2, exceptionString())
            #self.log(2, "-ERR " + str(msg))
        wf.flush()
        if cmd == "QUIT":
            wf.close()
            self.server.close_request(self.request)
            return


</t>
<t tx="T172">
class POPSession:
    @others
</t>
<t tx="T173"># states
Q_AUTHORIZATION_USER = "AUTHORIZATION (USER)"
Q_AUTHORIZATION_PASS = "AUTHORIZATION (PASS)"
Q_TRANSACTION = "TRANSACTION"

</t>
<t tx="T174">def __init__(self, wf, freemail):
    self.write = wf.write
    self.state = POPSession.Q_AUTHORIZATION_USER
    self.username = None
    self.freemail = freemail
    self.log = freemail.log

</t>
<t tx="T175">def write_ok(self, msg):            # sends an OK response.
    self.log(5, "+OK " + str(msg))
    self.write("+OK %s\r\n" % msg)

</t>
<t tx="T176">def write_multi(self, lines):       # write "byte-stuffed" lines.
    for s in lines:
        if s[:1] == ".":            # termination octet?
            s = "." + s
        self.write(s + "\r\n")
</t>
<t tx="T177">def write_raw(self,str):
    #print str
    self.write(str)
</t>
<t tx="T178">def __check_state(self, STATE):
    if self.state is not STATE:
        raise Error, "Wrong state " + self.state
</t>
<t tx="T179">def CAPA(self):
    self.write_ok("Good bye...")
</t>
<t tx="T180">def QUIT(self):
    if self.state is POPSession.Q_TRANSACTION:
        self.maildrop.update()
    self.write_ok("Good bye...")
</t>
<t tx="T181">def USER(self, username):
    self.__check_state(POPSession.Q_AUTHORIZATION_USER)
    if not self.freemail.check_user(username):
        raise Error, "Unknown user %s" % username
    self.username = username
    self.state = POPSession.Q_AUTHORIZATION_PASS
    self.write_ok("User %s accepted" % username)
</t>
<t tx="T182">def PASS(self, password):
    self.__check_state(POPSession.Q_AUTHORIZATION_PASS)

    if self.freemail.auth(self.username, password) == "":
        self.state = POPSession.Q_AUTHORIZATION_USER
        POP3Logger("Failure to login as " + self.username, loglevel=1)
        raise Error, "Invalid password"

    self.username = self.freemail.check_user(self.username)

    self.maildrop = POPMaildrop(self)
    self.state = POPSession.Q_TRANSACTION
    POP3Logger("Successful login as " + self.username, loglevel=1)
    self.write_ok("Password accepted")
</t>
<t tx="T183">def STAT(self):

    self.__check_state(POPSession.Q_TRANSACTION)

    self.write_ok("%d %d" % (self.maildrop.msgcount,
                             self.maildrop.mailboxsize)
                  )
</t>
<t tx="T184">def LIST(self, msgno=None):

    self.__check_state(POPSession.Q_TRANSACTION)

    self.maildrop.update()
    mails = self.maildrop.mails

    self.log(5, "msgno = '%s'" % msgno)

    #print "MAILS:"
    #print mails

    self.log(5, "got LIST for user %s" % self.username)
        
    if msgno is None:
        self.write_ok("scan listing begins")
        count = 0
        for (status, size, msg) in mails:
            count += 1
            line = "%d %d" % (count, size)
            self.write(line + "\r\n")
            #mydebug( line)
        self.write(".\r\n")
    else:
        try:
            msg = mails[_get_no(msgno)][2]
            self.write_ok("%s %s" % (msgno, msg.msgHash))
        except:
            self.log(2, "Exception retrieving mail list:\nmsgno=%s\nmsg=%s\n%s" % (msgno, msg, exceptionString()))
            raise

</t>
<t tx="T185">def RETR(self, msgno):

    self.__check_state(POPSession.Q_TRANSACTION)

    msg = self.maildrop.get_msg(_get_no(msgno))
    #print "STATUS:*%s*" % self.maildrop.get_msg_status(_get_no(msgno))
    #print "MSG:\r\n",msg
    #lines = msg.split("\r\n")
    self.write_ok("Sending message")
    self.write_raw(msg)
    self.write_raw("\r\n.\r\n")
</t>
<t tx="T186">def DELE(self, msgno):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.maildrop.delete_msg(_get_no(msgno))
    self.write_ok("Message deleted")
</t>
<t tx="T187">def NOOP(self):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.write_ok("Still here...")
</t>
<t tx="T188">def RSET(self):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.maildrop.reset()
    self.write_ok("Messages unmarked")</t>
<t tx="T189">def TOP(self, msgno, n):

    self.__check_state(POPSession.Q_TRANSACTION)
    try:
        n = int(n)
    except ValueError:
        raise Error, "Not a number"
    msg = self.maildrop.get_msg(_get_no(msgno))
    [head, body] = msg.split("\r\n\r\n", 1)
    self.write_ok("Top of message follows")
    self.write_multi(head.split("\r\n"))
    self.write("\r\n")
    self.write_multi(body.split("\r\n")[:n])
    self.write(".\r\n")
</t>
<t tx="T190">def UIDL(self, msgno=None):

    self.__check_state(POPSession.Q_TRANSACTION)
    if msgno is None:
        self.write_ok("UIDL listing begins") 
        count = 0           
        for (status, size, msg) in self.maildrop.mails:
            count += 1
            line = "%d %s" % (count, _digest(str(msg)))
            self.write(line + "\r\n")
            mydebug( line)
        self.write(".\r\n")
    else:
        msg = self.maildrop.get_msg(_get_no(msgno))
        self.write_ok("%s %s" % (msgno, _digest(msg)))
</t>
<t tx="T191">class POPMaildrop:
    @others
</t>
<t tx="T192">def __init__(self, popsess):

    freemail = popsess.freemail
    self.username = popsess.username
    self.freemail = freemail
    #self.maildrop = self
    self.db = freemail.db
    db = self.db
    self.log = freemail.log

    self._dbLock = freemail._dbLock
    self._dbUnlock = freemail._dbUnlock

    self.update()

    return
</t>
<t tx="T193">def get_stat(self):
    return self.msgcount, self.mailboxsize
</t>
<t tx="T194">def get_msg(self,msgno):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    freemail = self.freemail
    db = freemail.db
    config = db.config

    msgpath = os.path.join(config.storeDir, "rx", self.mails[msgno][2].msgHash)
    fd = open(msgpath, "rb")
    msg = fd.read()
    fd.close()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    return msg

    # old stuff from pop3 maildrop
    msgfile = open(self.maildir+"/"+self.mails[msgno][2], "r")
    msg = msgfile.read()
    msgfile.close()
    
    freemail.log(4, "*** Body of message follows...")
    freemail.log(4, msg)

    return msg

</t>
<t tx="T195">def delete_msg(self, msgno):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    try:
        db = self.db

        self.log(5, "Want to delete msg:\nmsgno=%s\nidAddr=%s\nself.mails=%s" % (
                        msgno, self.username, self.mails))

        msg = self.mails[msgno][2]
        msg.isDeleted = 1
    
        self.log(4, "Marking message %d as deleted" % msgno)
    
        #del self.mails[msgno]
    
    except:
        self.log(2, "Error deleting msg:\nmsgno=%s\nidAddr=%s\nself.mails=%s\n%s" % (
                        msgno, self.username, self.mails, exceptionString()))

    self.freemail.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    return</t>
<t tx="T196">def update(self):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    freemail = self.freemail
    db = self.db
    config = db.config

    # get a view of messages targetted at user
    vMsgs = db.rxMessages.select(idAddr=self.username, isDeleted=0)

    self.mails = []
    msgcount = 0
    totalsize = 0
    for rMsg in vMsgs:
        if not rMsg.isDeleted:
            itemsize = rMsg.msgLen
            totalsize += itemsize
            msgcount += 1
            self.mails.append( (0, itemsize, rMsg) )
    self.msgcount = msgcount 
    self.mailboxsize = totalsize

    freemail.log(5, "POPMaildrop: storeDir='%s'" % config.storeDir)
    self.maildir = os.path.join(config.storeDir, "rx")

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------


</t>
<t tx="T197">def reset(self):
    pass
</t>
<t tx="T198">def POP3Logger(message, loglevel=3):
    return
    print "pop3: %s" % message
</t>
<t tx="T199">def mydebug(msg):
    print msg
</t>
<t tx="T200">class Error (Exception):
    pass
</t>
<t tx="T201">def _digest(msg):
    i = msg.find("\r\nMessage-")
    if i &gt;= 0:
        if msg[i+10: i+14].upper() == "ID: ":
            j = msg.find("\r\n", i+14)
            if j &gt;= 0:
                msgid = msg[i+14: j]
                if len(msgid) &gt; 10:
                    return md5.new(msgid).hexdigest()
    return md5.new(msg).hexdigest()
</t>
<t tx="T202">def _get_no(msgno):
    try:
        return int(msgno) - 1
    except ValueError:
        raise Error, "Not a number"
</t>
<t tx="T203"></t>
<t tx="T204">class SMTPServer:
    """
    A single threaded SMTP Server connection manager. Listens for
    incoming SMTP connections on a given port. For each connection,
    the SMTPSession is chugged, passing the given instance of
    SMTPServerInterface. 
    """
    @others
</t>
<t tx="T205">def __init__(self, **kw):
    
    allowedHosts = kw.get('allowedHosts', None)
    if allowedHosts:
        bindAddr = ''
    else:
        bindAddr = '127.0.0.1'
        allowedHosts = ['127.0.0.1']
    self.allowedHosts = allowedHosts

    port = kw.get('port', 25)
    self.port = port

    self.freemail = kw['freemail']

    self._socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    self._socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #self._socket.bind((socket.INADDR_ANY, port))

    self._socket.bind((bindAddr, port))
    self._socket.listen(5)

    # do a lookup on all the local domains
    localIPs = [socket.gethostbyname(domain) for domain in allowedHosts]
    if '127.0.0.1' not in localIPs:
        localIPs.append('127.0.0.1')

    self.localIPs = localIPs

</t>
<t tx="T206">def run(self):

    print "** Freemail SMTP server now listening on port %d" % self.port

    while 1:
        try:
            nsd = self._socket.accept()
        except socket.timeout:
            continue

        # check if host is on 'allowd hosts' list
        remaddr = nsd[1][0]
        if remaddr not in self.freemail.db.smtpHosts:
            self.freemail.log(1, "Terminating connection from unauthorised host at %s" % remaddr)
            nsd[0].close()
            continue

        session = SMTPSession(nsd[0], nsd[1], self.freemail, self.allowedHosts)
        thread.start_new_thread(session.chug, ())
        #session.chug()
</t>
<t tx="T207">


#
# This drives the state for a single RFC821 message.
#
class SMTPSession:
	&lt;&lt; class SMTPSession declarations &gt;&gt;
    @others
</t>
<t tx="T208">"""
Server engine that calls methods on the SMTPServerInterface object
passed at construction time. It is constructed with a bound socket
connection to a client. The 'chug' method drives the state,
returning when the client RFC821 transaction is complete. 
"""

ST_INIT = 0
ST_HELO = 1
ST_MAIL = 2
ST_RCPT = 3
ST_DATA = 4
ST_QUIT = 5

</t>
<t tx="T209">def __init__(self, socket, addr, freemail, allowedIPs):
    # initialisations from old engine class
    self.tid = getNewTid()
    self.socket = socket;
    self.addr = addr[0]
    self.freemail = freemail
    self.db = freemail.db
    self._dbLock = freemail._dbLock
    self._dbUnlock = freemail._dbUnlock
    self.log = freemail.log
    self.state = SMTPSession.ST_INIT
    self.allowedIPs = allowedIPs

    # initialisations from 'implementation' class
    self.remaddr = addr[0]
    self.sock = socket
    self.savedTo = []
    self.toAddrs = []
    self.savedHelo = ''
    self.savedMailFrom = ''
    #self.savedData = ''
    self.shutdown = 0
    self.isrbl = 0
    
    self.log(3, "SMTPSession.__init__: pid=%s, remaddr=%s" % (os.getpid(), self.addr))

    # determine if remote MUA/MTA is running on local machine
    self.senderIsLocal = (1 in [fnmatch.fnmatch(self.addr, patt) for patt in allowedIPs])

    if self.senderIsLocal:
        self.log(4, "Sender at %s is local" % self.addr)
    else:
        self.log(4, "Sender at %s is REMOTE" % self.addr)
</t>
<t tx="T210">def chug(self):
    """
    Chug the engine, till QUIT is received from the client. As
    each RFC821 message is received, calls are made on the
    SMTPServerInterface methods on the object passed at
    construction time.
    """
    self.logStatus(4, "Got connection from '%s'" % self.addr)

    self.socket.send("220 Welcome to the Freemail SMTP Server\r\n")
    try:
        while 1:
            self.socket.setblocking(0)
            try:
                data = self.socket.recv(1024);
                self.socket.setblocking(1)
            except:
                self.logStatus(5, "waiting for data on socket")
                time.sleep(1)
                continue

            #print "GOT DATA: '%s'" % data

            if len(data):
                if self.state != SMTPSession.ST_DATA:
                    rsp, keep = self.doCommand(data)
                    self.socket.send(rsp + "\r\n")
                else:
                    rsp = self.doData(data)
                    if rsp == None:
                        continue
    
                    self.logStatus(5, "message is not spam")
                    self.socket.send("250 OK - Data and terminator. found\r\n")
    
                if keep == 0:
                    self.socket.close()
                    break
            else:
                break;

        self.logStatus(4, "Session ended")
        #raise Exception("Testing exception logging")
        return
    except:
        self.logException(self.addr)
        self.logStatus(4, "Session ended after crash")

</t>
<t tx="T211">def doCommand(self, data):

    """Process a single SMTP Command"""
    cmd = data[0:4]
    cmd = string.upper(cmd)
    self.keep = 1
    rv = None
    if cmd == "HELO":
        self.state = SMTPSession.ST_HELO
        rv = self.helo(data)
    elif cmd == "RSET":
        rv = self.reset(data)
        self.dataAccum = ""
        if self.state != SMTPSession.ST_HELO:
            self.state = SMTPSession.ST_INIT
    elif cmd == "NOOP":
        pass
    elif cmd == "QUIT":
        rv = self.quit(data)
        self.keep = 0
    elif cmd == "MAIL":
        if self.state != SMTPSession.ST_HELO:
            return ("503 Bad command sequence", 1)
        self.state = SMTPSession.ST_MAIL
        rv = self.mailFrom(data)
    elif cmd == "RCPT":
        if (self.state != SMTPSession.ST_MAIL) and (self.state != SMTPSession.ST_RCPT):
            return ("503 Bad command sequence", 1)
        self.state = SMTPSession.ST_RCPT
        rv = self.rcptTo(data)
    elif cmd == "DATA":
        if self.state != SMTPSession.ST_RCPT:
            return ("503 Bad command sequence", 1)
        self.state = SMTPSession.ST_DATA
        self.dataAccum = ""
        return ("354 OK, Enter data, terminated with a \\r\\n.\\r\\n", 1)
    else:
        return ("505 Eh? WTF was that?", 1)

    if rv:
        return (rv, self.keep)
    else:
        return("250 OK", self.keep)
</t>
<t tx="T212">def doData(self, data):
    """
    Process SMTP Data. Accumulates client DATA until the
    terminator is found.
    """
    self.dataAccum = self.dataAccum + data
    if len(self.dataAccum) &gt; 4 and self.dataAccum[-5:] == '\r\n.\r\n':
        self.dataAccum = self.dataAccum[:-5]
        rv = self.data(self.dataAccum)
        self.state = SMTPSession.ST_HELO
        if rv:
            return rv
        else:
            return "250 OK - Data and terminator. found"
    else:
        return None
</t>
<t tx="T213">def logStatus(self, level, msg):
    msg = "%s: %s" % (self.tid, msg)
    self.freemail.log(level, msg)
</t>
<t tx="T214">def logException(self, addr):
    fd = StringIO("%s:" % self.tid)
    fd.write("Thread crash!!\n")
    fd.write("Addr = '%s'\n" % addr)
    traceback.print_exc(file=fd)
    self.freemail.log(1, fd.getvalue())
</t>
<t tx="T215">def helo(self, args):
    self.savedHelo = args
    self.logStatus(3, "SMTPService.helo: got '%s'" % args.strip())

    addr = self.remaddr

</t>
<t tx="T216">def mailFrom(self, args):
    # Stash who its from for later
    self.logStatus(3, "SMTPService.mailFrom: got '%s'" % args.strip())
    self.MAIL = args
    try:
        self.savedMailFrom = stripAddress(args)
        self.log(4, "from: '%s'" % self.savedMailFrom)
    except:
        self.savedMailFrom = args
        self.log(4, "stripAddress failed with '%s'" % args)

    # check the FROM user is known to us
    if self.freemail.db.identities.has_key(self.savedMailFrom):
        return "250 OK got valid MAIL FROM address"
    else:
        self.keep = 0
        return "553 Bad 'FROM:' address '%s'" % self.savedMailFrom

</t>
<t tx="T217">def rcptTo(self, args):

    # Stashes multiple RCPT TO: addresses
    self.logStatus(3, "SMTPService.rcptTo: got '%s'" % args.strip())

    # save the command verbatim for later passing to MTA
    self.savedTo.append(args)

    # extract the target email address
    try:
        toAddr = stripAddress(args)
    except:
        self.logStatus(2, "failed to get 'To' address from '%s'" % args.strip())
        self.keep = 0
        return "553 Invalid RCPT syntax: '%s'" % args

    # parse recipient address
    addrBits = toAddr.split("@", 1)
    if len(addrBits) &lt; 2:
        addrBits.append("127.0.0.1")
    toUser, toDomain = addrBits[0], addrBits[1]

    # barf on invalid address format
    if not toUser:
        self.logStatus(4, "Invalid recipient address '%s'" % toAddr)
        self.keep = 0
        return "553 Invalid recipient address '%s'" % toAddr
    
    # attempt lookup on recipient domain
    try:
        toIP = socket.gethostbyname(toDomain)
    except:
        toIP = ''

    # barf if lookup failed
    if 0 and not toIP:
        self.logStatus(4, "DNS lookup on domain of '%s' failed" % toAddr)
        self.keep = 0
        return "553 Name lookup on domain '%s' failed" % toDomain

    # check that either recipient or sender is local
    if not (self.senderIsLocal or (toIP in localIPs)):
        # someone is trying to relay through us
        self.logStatus(4, "Peer at %s is trying to relay to %s" % (self.addr, toAddr))
        self.keep = 0
        return "553 Server not available for relaying"

    # all is ok
    self.logStatus(4, "Got valid recipient address: %s" % toAddr)
    self.toAddrs.append(toAddr)
    return "250 Recipient address OK"

</t>
<t tx="T218">def data(self, args):
    # Process client mail data. It inserts a silly X-Header, then
    # does a MX DNS lookup for each TO address stashed by the
    # rcptTo method above. Messages are logged to the console as
    # things proceed. 
    self.logStatus(6, "SMTPService.data: got '%s'" % args)

    if args[-1] != '\n':
        self.logStatus(4, "Appending newline to data")
        args = args + "\n"
    self.rawData = args

    self.dispatchMessage()

    return "250 OK - Data and terminator. found"
    #return None
</t>
<t tx="T219">def quit(self, args):
    self.logStatus(5, "HELO: '%s'" % self.savedHelo)
    self.logStatus(5, "MAIL: '%s'" % self.savedMailFrom)
    self.logStatus(5, "RCPT: '%s'" % self.savedTo)
    try:
        self.logStatus(5, "DATA: '%s'" % self.savedData)
    except:
        self.logStatus(3, "QUIT: no saved data")
    self.logStatus(4, "Session with '%s' complete" % str(self.remaddr))
    self.savedTo = []
</t>
<t tx="T220">def reset(self, args):
    self.logStatus(5, 'Received "RSET": "%s"' % args)
    pass

</t>
<t tx="T221">def dispatchMessage(self):

    self.logStatus(4, "GOT MESSAGE TO SEND!")
    self.logStatus(4, "From: '%s'" % self.savedMailFrom)
    self.logStatus(4, "To: '%s'" % ",".join(self.toAddrs))
    self.logStatus(4, "RAW DATA:\n%s" % self.rawData)

    for toAddr in self.toAddrs:
        self.freemail.enqueueMessage(self.savedMailFrom,
                                     toAddr,
                                     'message',
                                     self.rawData)
</t>
<t tx="T222"></t>
<t tx="T223">class TelnetServer:
    """
    Implements the FreeMail telnet interface
    """
    @others
</t>
<t tx="T224">def __init__(self, **kw):
    """
    Creates a POP server.
    
    No arguments.
    
    Keywords:
     - freemail - a freemailServer object - compulsory
     - bindaddr - TCP bind address, default '127.0.0.1'
     - port - default 110
     - handlerClass - a class constructor for a Telnet Request Handler
    """
    self.freemail = kw['freemail']
    self.bindaddr = kw.get('bindaddr', '')
    self.port = kw.get('port', 10021)
    self.handlerClass = TelnetRequestHandler
</t>
<t tx="T225">def exitNormal(self, signum, frame):
    sys.exit(0)
</t>
<t tx="T226">def run(self):

    #try:
    #    signal.signal(signal.SIGTERM, self.exitNormal)
    #    signal.signal(signal.SIGABRT, self.exitNormal)
    #except:
    #    print "Can't hook signals on this platform"

    while 1:
        try:
            self.server = SocketServer.ThreadingTCPServer(
                (self.bindaddr, self.port),
                self.handlerClass,
                )
            break
        except:
            print "Can't bind to telnet socket - port %s - waiting to retry..." % self.port
            time.sleep(10)

    self.server.socket.setsockopt(
        socket.SOL_SOCKET,
        socket.SO_REUSEADDR,
        1)

    self.server.freemail = self.freemail
    self.server.gotClient = 0

    print "** Freemail Telnet Server now listening on port %d" % self.port

    while 1:
        self.server.handle_request()</t>
<t tx="T227">class TelnetRequestHandler(SocketServer.StreamRequestHandler):
    @others
</t>
<t tx="T228">def __init__(self, sock, addr, server, **kw):

    if server.gotClient:
        sock.send("You are logged in elsewhere!\r\n")
        server.close_request(server.request)
        return
    server.gotClient = 1

    self.freemail = server.freemail
    self.config = self.freemail.db.config
    self.log = self.freemail.log
    self.remotePeer = addr[0]

    #print "POPRequestHandler: args='%s' kw='%s'" % (str(args), str(kw))

    self.log(3, "POPRequestHandler: addr=%s" % str(addr))
    SocketServer.StreamRequestHandler.__init__(self, sock, addr, server, **kw)

</t>
<t tx="T229">def handle(self):

    wf = self.request.makefile('wb')

    if self.remotePeer not in self.freemail.db.telnetHosts:
        wf.write("You are not allowed to connect to FreeMail from this machine\n")
        self.server.close_request(self.request)
        self.server.gotClient = 0
        return

    wf.write("FreeMail Telnet Server ready\n")
    wf.flush()

    # wf.write

    config = self.config

    badlogins = 0
    loginState = 'login'
    while badlogins &lt; 5:
        while 1:
            wf.write("Login: ")
            wf.flush()
            login = self.getline()
            if login != '':
                break
        wf.write("Password: ")
        wf.flush()
        password = self.getline()
        if login.strip() != config.adminLogin or hash(password.strip()) != config.adminPassword:
            wf.write("Invalid login or password\n")
            badlogins += 1
        else:
            wf.write("You are now logged in\n")
            break

    if badlogins == 5:
        self.server.close_request(self.request)

    self.server.gotClient = 1
    sys.stdout = wf

    # get a console up
    console = freemailConsole(globals(), outfile=wf)
    
    while 1:
        try:
            wf.write(console.prmt)
            wf.flush()
            
            line = self.getline()
            if line == '':
                wf.close()
                break

            self.freemail.log(5, "TelnetRequestHandler: line='%s'" % line.strip())
            
            console.handle_line(line)
        except:
            self.freemail.log(5, "TelnetRequestHandler: exception:\n%s'" % exceptionString())
            break

    self.server.close_request(self.request)
    self.server.gotClient = 0
    sys.stdout = oldStdout
</t>
<t tx="T230">def getline(self):
    """
    Reads one line of text from remote peer
    """
    chars = []

    while 1:
        char = self.request.recv(1)
        #print "char = '%s'" % repr(char)
        if char == '':
            break
        if char == '\n':
            break
        chars.append(char)
    line = "".join(chars)
    return line
</t>
<t tx="T231"></t>
<t tx="T232">class freemailConsole(InteractiveConsole):

    helpDocs = {
'': """
FreeMail interactive console help.

Type 'help topic' to display detailed help on topic

Available topics:
  help commands verbosity identities reinsert objects
""",

'help': """
help

Synopsis:
    help [arg]
    
Displays help on one of the available help topics.
If arg is not one of these topics, the python help is invoked.
""",

'commands': """
FreeMail console commands summary:
    
    help - display help on topics
    commands - display a command summary
    verbosity - set/change logging verbosity level
    identities - display mailing identities and status
    reinsert - re-insert one or all mailing identities into freenet
    
""",

'verbosity': """
verbosity

Synopsis:
    verbosity [n]

Displays or sets the current logging verbosity level.
If no argument is given, displays the current verbosity.
If an arg is given, this becomes the new verbosity level.

Valid values for n are 0,...,5
""",

'identities': """
identities

Synopsis:
    identities

Displays a list of your currently active freemail identities, and
their reference numbers. (You need the reference number to operate
on any identity).
""",

'reinsert': """
reinsert

Synopsis:
    reinsert id-num
    reinsert all

If arg is a number, reinsert that identity into freenet. To see the
numbers corresponding to your identities, type 'identities'.

If arg is 'all', then schedule all current identities for reinsertion.
""",

'objects': """
Python objects available within this console:
    
    m - the currently running freemail server object
    db - the database
    conf - the configuration object (equiv to db.conf)
    
""",
}
                   
    def __init__(self, mylocals, **kw):

        self.hideSymbols(mylocals)
        InteractiveConsole.__init__(self, mylocals)

        self.outfile = kw.get('outfile', sys.stdout)
        self.setup()

    def hideSymbols(self, mylocals):
        for sym in mylocals.keys():
            if sym in ['help']:
                del mylocals[sym]
        
    def setup(self):

        self.ps1 = interactivePrompt
        self.ps2 = "   &lt;cont&gt; "
        self.prmt = self.ps1
        self.iscont = 0
        self.isrunning = 1
        
    def help(self, arg):
        
        if arg in self.helpDocs.keys():
            pydoc.pager(self.helpDocs[arg])
        else:
            self.push("help(%s)" % arg)

    def run(self):

        print
        print "************************************************************************"
        print "FreeMail (freenet mailserver) build %s" % build
        print "Interactive Python Console"
        print "FreeMail server object is 'm', database object is 'db', config object is 'conf'"
        print
        print "type 'dir(object) or help(object) to get help on object"
        print "type 'v(n) to set verbosity level (0 &lt;= n &lt;= 5)"
        print "************************************************************************"
        print

        while self.isrunning:
            try:
                line = self.raw_input(self.prmt)
            except EOFError:
                print
                break
            if self.handle_line(line) == 'quit':
                break

        if os.path.isfile("freemail.pid"):
            os.unlink("freemail.pid")
        print "Terminating FreeMail server..."

    def handle_line(self, line):
        #print "handle_line: line='%s'" % line
        if not self.iscont:
            res = self.handle_command(line)
            if res == 'quit':
                return 'quit'
            if res:
                cont = 1
                self.prmt = self.ps2
            else:
                cont = 0
                self.prmt = self.ps1
        else:
            if not self.push(line):
                cont = 0
                self.prmt = self.ps1
            else:
                cont = 1
                self.prmt = self.ps2
        
    def handle_command(self, line):

        #line = line.strip()
        try:
            if line[-1] == '\r':
                line = line[:-1]
        except:
            pass
        try:
            cmd, args = re.split("\\s+", line, 1)
        except:
            cmd = line
            args = ""

        if cmd == 'help':
            self.help(args)

        elif cmd == 'quit':
            return quit

        elif cmd == 'verbosity':
            if args:
                self.push("m.verbosity(%s)" % args)
            else:
                print "Current verbosity is: %s" % m._verbosity

        elif cmd == 'identities':
            idnames = m.db.identities.keys()
            idnames.sort()
            i = 0
            for id in idnames:
                print "%4s %s" % (i, id)
                rec = m.db.identities[id]
                if rec.idRefreshInProgress:
                    print "       Refresh currently in progress"
                else:
                    print "       Last refreshed: %s" % time.asctime(time.localtime(rec.idLastInserted))
                i += 1

        elif cmd == 'reinsert':
            if args == 'all':
                m.forceIdRefresh()
                print "Scheduled all identities for reinsertion"
            elif args == '':
                print "You must specify an identity to reinsert, or 'all'"
            else:
                idAddr = self.idByNum(args)
                if idAddr == None:
                    print "Invalid id '%s'" % args
                    print "Type 'identities' to see valid id numbers"
                else:
                    m.forceIdRefresh(idAddr)
                    print "Scheduled identity '%s' for reinsertion" % idAddr

        else:
            # execute anything else as a python command
            return self.push(line)

    def idByNum(self, n):
        try:
            idnames = m.db.identities.keys()
            idnames.sort()
            return idnames[int(n)]
        except:
            return None

    def write(self, buf):
        self.outfile.write(buf)
        self.outfile.flush()
</t>
<t tx="T233"></t>
<t tx="T234">class cell:
    
    """
    Generic storage object, a kind-of cross between an attributed object and a sequence,
    with some metakit-style methods
    """

    @others
</t>
<t tx="T235">def __init__(self, *args, **kw):
    """
    Creates a cell object.
    
    The keyword 'defaults' is a dict of default values
    """
    self._val = None
    self._seq = []
    self._dict = {}

    if len(args) == 1:
        arg = args[0]
        if type(arg) in [type([]), type(())]:
            self._seq = list(arg)
        elif type(arg) is type({}):
            self._dict = arg
        else:
            self._val = arg
    else:
        self._seq = list(args)

    if kw.has_key('defaults'):
        dflts = kw['defaults']
        if type(dflts) is not type({}):
            raise Exception("Constructor keywords 'defaults' must be a dict")
        del kw['defaults']
        self.__dict__['_defaults'] = dflts
    else:
        self.__dict__['_defaults'] = {}
                    
    self._dict.update(kw)
</t>
<t tx="T236">def __getattr__(self, attr):

    try:
        if not self.__dict__.has_key('_dict') or attr not in self._dict.keys():
            if attr not in ['__getinitargs__', '__getstate__', '__setstate__', '__iter__',
                            '__eq__', '__coerce__', '__cmp__']:
                #print "can't find attribute '%s'" % attr
                #print "Cell attribs: %s" % self._dict.keys()
                pass
            raise AttributeError("Cell object has no attribute '%s'" % attr)
    except AttributeError:
        if attr not in ['__getinitargs__', '__getstate__', '__setstate__', '__iter__',
                        '__eq__', '__coerce__', '__cmp__']:
            fd = open("freemail.log.crash", "ab")
            traceback.print_exc(file=fd)
            fd.close()
        raise

    return self._dict[attr]
</t>
<t tx="T237">def __setattr__(self, attr, val):

    self.__dict__[attr] = val
    if attr not in ['_val', '_seq', '_dict', '_defaults']:
        self._dict[attr] = val
</t>
<t tx="T238">def __delattr__(self, name):
    
    self._dict.__delattr__(name)
</t>
<t tx="T239">def __iter__(self):

    #return self._seq.__iter__
    return iter(self._seq)
</t>
<t tx="T240">def __delattr__(self, attr):

    if not self._dict.has_key(attr):
        raise AttributeError("cell object has no attribute '%s'" % attr)

    del self._dict[attr]
</t>
<t tx="T241">def __getitem__(self, idx):

    try:
        return self._seq[idx]
    except:
        return self.__getattr__(idx)
</t>
<t tx="T242">def __setitem__(self, idx, val):

    try:
        self._seq[idx] = val
    except:
        self.__setattr__(idx, val)
</t>
<t tx="T243">def __delitem__(self, idx):
    try:
        self._seq.__delitem__(idx)
    except TypeError:
        del self._dict[idx]
</t>
<t tx="T244">def __getslice__(self, fromidx, toidx):
    
    return self._seq[fromidx:toidx]
</t>
<t tx="T245">def __setslice__(self, fromidx, toidx, newslice):
    
    self._seq[fromidx:toidx] = newslice
</t>
<t tx="T246">def __nonzero__(self):
    return (len(self._seq) &gt; 0)
</t>
<t tx="T247">def __len__(self):
    
    return len(self._seq)
</t>
<t tx="T248">def __str__(self):

    return self.dump()
</t>
<t tx="T249">def __repr__(self):
    return self.dump()
</t>
<t tx="T250">def append(self, *items, **kw):

    for item in items:
        if item is not None:
            if item.__class__ is not cell:
                pass
                #raise Exception("Args to append must be cell objects")
            else:
                item.setDefaults(self._defaults)
            self._seq.append(item)

    if kw != {}:
        c = cell(**kw)
        c.setDefaults(self._defaults)
        self._seq.append(c)
</t>
<t tx="T251">def dump(self):
    """
    Dumps out attribs
    """
    rep = "{"
    items = []
    for k,v in self._dict.items():
        items.append("%s: %s" % (repr(k), repr(v)))
    rep += ", ".join(items)
    rep += "}"
    rep += repr(self._seq)
    return rep
</t>
<t tx="T252">def dumpseq(self):
    """
    Dumps out sequence
    """
</t>
<t tx="T253">def extend(self, lst):
    
    self._seq.extend(lst)
</t>
<t tx="T254">def getColAsList(self, colname):

    lst = []
    for item in self._seq:
        lst.append(getattr(item, colname))
    return lst
</t>
<t tx="T255">def has_key(self, name):
    return self._dict.has_key(name)
</t>
<t tx="T256">def index(self, item):
    
    return self._seq.index(item)
</t>
<t tx="T257">def insert(self, idx, obj):
    
    return self._seq.insert(idx, obj)
</t>
<t tx="T258">def items(self):
    return self._dict.items()
</t>
<t tx="T259">def join(self, other, *attribs):
    """
    Returns a cell containing a sequence of joined rows of this and other cell
    """
    if len(attribs) == 1:
        if type(attribs[0]) in [type([]), type(())]:
            attribs = attribs[0]
    newcell = cell()
    for item in self._seq:
        itemdict = item._dict
        itemdictkeys = itemdict.keys()
        for item1 in other._seq:
            item1dict = item1._dict
            item1dictkeys = item1dict.keys()
            matches = 1
            for attr in attribs:
                if not (attr in itemdictkeys \
                        and attr in item1dictkeys \
                        and itemdict[attr] == item1dict[attr]):
                    matches = 0
                    break
            if matches:
                # create cell with attribs of both items
                newitem = cell()
                for attr in itemdictkeys:
                    setattr(newitem, attr, itemdict[attr])
                for attr in item1dictkeys:
                    setattr(newitem, attr, item1dict[attr])
                newcell.append(newitem)
    return newcell
</t>
<t tx="T260">def keys(self):
    return self._dict.keys()
</t>
<t tx="T261">def onlyWhen(self, func):
    newcell = cell()
    newcell._seq = filter(func, self._seq)
    return newcell
</t>
<t tx="T262">def pop(self, idx=None):
    
    if idx is None:
        return self._seq.pop()
    else:
        return self._seq.pop(idx)
</t>
<t tx="T263">def project(self, *attribs):
    """
    Returns a cell object, whose list members contain only the attributes
    in attribs
    """
    if len(attribs) == 1:
        if type(attribs[0]) in [type([]), type(())]:
            attribs = attribs[0]
    newcell = cell()
    for item in self._seq:
        newitem = cell()
        for attr in attribs:
            setattr(newitem, attr, getattr(item, attr))
        newcell.append(newitem)
    return newcell
</t>
<t tx="T264">def remove(self, value):
    
    self._seq.remove(value)
</t>
<t tx="T265">def removeWhen(self, func=None, **kw):
    if func:
        def _shouldKeep(item, func=func):
            return not func(item)
        self._seq = filter(_shouldKeep, self._seq)
    else:
        def _shouldKeep(item, kw=kw):
            #print kw
            for attr, val in kw.items():
                if not (hasattr(item, attr) and getattr(item, attr) == val):
                    return 1
            return 0
        self._seq = filter(_shouldKeep, self._seq)
</t>
<t tx="T266">def reverse(self):
    
    self._seq.reverse()
</t>
<t tx="T267">def select(self, **kw):

    def _select(item, kw=kw):
        #print kw
        for attr, val in kw.items():
            if not (hasattr(item, attr) and getattr(item, attr) == val):
                return 0
        return 1

    return cell(filter(_select, self._seq))
</t>
<t tx="T268">def setDefaults(self, defaults):
    """
    Create a new cell, containing all the attribs of the old one,
    with the defaults filled in
    """
    d = self._dict
    for k, v in defaults.items():
        if not d.has_key(k):
            d[k] = v
</t>
<t tx="T269">def sort(self, *attribs):
    """
    Sorts a cell's sequence
    """
    if len(attribs) == 1:
        if type(attribs[0]) in [type([]), type(())]:
            attribs = attribs[0]

    def sortfunc(item1, item2, attribs=attribs):
        for attr in attribs:
            has1 = hasattr(item1, attr)
            has2 = hasattr(item1, attr)

            # judge first on presence of attribs
            if has1 and not has2:
                return 1
            elif has2 and not has1:
                return -1
            
            # now judge on values
            if (has1 and has2):
                val1 = getattr(item1, attr)
                val2 = getattr(item2, attr)
                if val1 &lt; val2:
                    return -1
                elif val1 &gt; val2:
                    return 1
        
        # exhausted sort attribs - judge as identical
        return 0

    self._seq.sort(sortfunc)
</t>
<t tx="T270">def unique(self):
    """
    Returns a copy of this cell, with duplicate sequence members removed
    """
    newcell = cell()
    seq = self._seq
    seqlen = len(seq)
    i = 0
    while i &lt; seqlen:
        isunique = 1
        j = i
        while j &lt; seqlen:
            if seq[i]._dict == seq[j]._dict:
                isunique = 0
                break
            j = j + 1
        if isunique:
            newcell.append(seq[i])
        i = i + 1
    newcell._seq.reverse()
    return newcell
</t>
<t tx="T271">def values(self):
    return self._dict.values()
</t>
<t tx="T272">class slotmap:
    """
    Implements a kind of sparse list for queue slots
    """

    def __init__(self):

        self.slots = {}

    def __getitem__(self, idx):

        # will always work - creates an item if one doesn't already exist
        if not self.slots.has_key(idx):
            self.slots[idx] = SLOT_STATE_EMPTY
        return self.slots[idx]

    def __repr__(self):
        return repr(self.slots)
    
    def __str__(self):
        return str(self.slots)

    def __setitem__(self, idx, val):
        self.slots[idx] = val

    def keys(self):
        return self.slots.keys()

    def values(self):
        return self.slots.values()

    def items(self):
        return self.slots.items()

    def between(self, fromidx, toidx):
        keys = range(fromidx, toidx)
        return filter(lambda r,myself=self: myself[r] == SLOT_STATE_EMPTY,
                      keys)

    def resetBusy(self):
        s = self.slots
        for k in s:
            if s[k] == SLOT_STATE_BUSY:
                s[k] = SLOT_STATE_EMPTY

    def purgeOld(self, idx):

        for item in self.slots.keys():
            if item &lt; idx:
                del self.slots[item]
</t>
<t tx="T273">class DynamicSemaphore(threading._Semaphore):

    """
    Extension of Semaphore that provides for dynamic resizing of
    the semaphore.
    """

    def __init__(self, value=1, verbose=None):
        """
        store an 'initvalue' attribute, so we can accurately
        frig the '__value' field.
        """
        threading._Semaphore.__init__(self, value, verbose)
        self.maxvalue = value

    def resize(self, newvalue=1):
        """
        Sets a new limit on the number of unreleased 'acquire()'s
        """
        nOut = self.maxvalue - self._Semaphore__value
        self.maxvalue = newvalue
        self._Semaphore__value = newvalue - nOut

    def acquire(self, blocking=1):
        rc = 0
        self._Semaphore__cond.acquire()
        while self._Semaphore__value &lt;= 0:
            if not blocking:
                break
            self._Semaphore__cond.wait()
        else:
            self._Semaphore__value = self._Semaphore__value - 1
            rc = 1
        self._Semaphore__cond.release()
        return rc

    def isempty(self):
        return (self._Semaphore__value == self.maxvalue)

    def release(self):
        self._Semaphore__cond.acquire()
        self._Semaphore__value = self._Semaphore__value + 1
        self._Semaphore__cond.notify()
        self._Semaphore__cond.release()

    def _dump(self):
        print "DynamicSemaphore: __value='%d', maxvalue='%d'" % (self._Semaphore__value,
                                                                  self.maxvalue)
</t>
<t tx="T274"></t>
<t tx="T275">def b64enc(s):
    """
    Returns a base-64 encoding of a string, all one one line
    """
    return base64.encodestring(s).replace("\n", "")

def b64dec(s):
    """
    Decodes a single-line base64 encoded string
    """
    return base64.decodestring(s)
</t>
<t tx="T276">def dhms(secs):
    """
    Formats a number of seconds into a human readable string,
    'n days, n hours, n minutes, n seconds
    """
    days, rest = divmod(secs, 86400)
    hours, rest = divmod(rest, 3600)
    minutes, rest = divmod(rest, 60)
    seconds = rest
    return "%d days, %d hours, %d minutes, %d seconds" % (days, hours, minutes, seconds)
</t>
<t tx="T277">def dbrStartTime(interval=86400, nperiods=0):
    """
    Returns the seconds since epoch at which a dbr period began.
    
    Arguments:
        - interval - the length of the dbr interval in seconds, default 1 day
        - nperiods - the number of periods to go *forward*
    """
    now = time.time()
    then = now + nperiods * interval
    then = then - (then % interval)
    return then


</t>
<t tx="T278">def exceptionString():
    
    """
    Gets a traceback of the last exception and returns it as a string
    Also writes the stacktrace to 'freemail.log.crash'
    """
    
    s = StringIO()
    traceback.print_exc(file=s)
    dump = s.getvalue()

    exceptionLogLock.acquire()
    try:
        fd = open("freemail.log.crash", "a")
        fd.write("%s\n" % dump)
        fd.close()
    except:
        pass
    exceptionLogLock.release()

    return dump
 
</t>
<t tx="T279">def stackString():
    
    """
    Gets a traceback of the stack
    """
    
    s = StringIO()
    traceback.print_stack(file=s)
    return s.getvalue()
 
</t>
<t tx="T280">def stripAddress(address):
    """
    Strip the leading &amp; trailing &lt;&gt; from an address.  Handy for
    getting FROM: addresses.
    """
    #start = string.index(address, '&lt;') + 1
    #end = string.index(address, '&gt;')
    #return address[start:end]

    patt = "(mail|Mail|MAIL|rcpt|Rcpt|RCPT)(\\s+)(from|From|FROM|to|To|TO):(\\s*)(.+)"
    address = string.strip(address)
    address = re.findall(patt, address)
    #print address
    address = address[0][4]
    address = address.strip()
    if address[0] == '&lt;':
        address = address[1:]
    if address[-1] == '&gt;':
        address = address[:-1]
    addrbits = address.split("@")
    if len(addrbits) == 2:
        name, domain = addrbits
        if name[0] == '"' and name[-1] == '"':
            name = name[1:-1]
        address = name.strip() + "@" + domain.strip()
    return address
</t>
<t tx="T281">def hash(raw):
    """
    Hashes a string into a hex string
    """
    #return re.sub("[=]*\s*", "", base64.encodestring(sha.new(raw).digest()))
    return sha.new(raw).hexdigest()
</t>
<t tx="T282">def randstring():
    """
    Outputs a 16-char random string
    """
    s = []
    random.seed(time.time())
    for i in range(32):
        s.append(chr(random.randint(ord("A"), ord("Z"))))
    s = "".join(s)
    return s
    return hash(s)
</t>
<t tx="T283">def getNewTid():
    global newTid
    tid = newTid
    newTid = newTid + 1
    return tid
</t>
<t tx="T284">def ulFromList(lst):
    u = ul()
    for item in lst:
        u.add(li(item))
    return u
</t>
<t tx="T285">def freemailAddrToUri(addr):
    """
    Converts a freemail address to the URI of the corresponding mailsite

    For example:
     - fred@blahblah.freemail =&gt; SSK@blahblahPAgM/freemail/fred

    Returns a freenet.uri object
    """
    try:
        name, rest = addr.split("@")
        hash, domain = rest.split(".")
        valid = 1
    except:
        valid = 0

    if not valid:
        raise Exception("Invalid freemail address syntax '%s'" % addr)

    uri = freenet.uri("SSK@%sPAgM/freemail/%s" % (hash, name))
    return uri
</t>
<t tx="T286">def uriToFreemailAddr(uri):
    """
    Converts a mailsite URI to a freemail address

    For example:
     - SSK@blahblahblahPAgM/freemail/alice[//] =&gt; alice@blahblahblah.freemail

    The uri can be expressed as a string, or a freenet.uri object
    """
    olduri = uri
    uri = freenet.uri(str(uri))
    hash = uri.hash
    if hash.endswith("PAgM") or hash.endswith("BCMA"):
        hash = hash[:-4]
    mskpath = uri.mskpath
    sskpath = uri.sskpath.split("/")
    
    if mskpath:
        raise Exception("Bad mailsite URI '%s' - msk path non-empty" % olduri)

    if uri.type != 'SSK':
        raise Exception("Bad mailsite URI '%s' - it's not an SSK" % olduri)

    if len(sskpath) != 2:
        raise Exception("Bad mailsite URI '%s' - should be 2 elements in SSK path" % olduri)
    if sskpath[0] != 'freemail':
        raise Exception("Bad mailsite URI '%s' - SSK path should begin with '/freemail'" % olduri)

    name = sskpath[1]
    
    return "%s@%s.freemail" % (name, hash)
</t>
<t tx="T287">def btnForm(label, action="", **kw):
    """
    Returns a styled button in its own form
    """
    butform = form(attr(method="POST", action=action),
                   btn(label),
                   )
    for k,v in kw.items():
        butform.add(input(type='hidden', name=k, value=v))

    return butform
</t>
<t tx="T288">def btn(label):
    """
    Creates and returns a styled text button

    Arguments:
     - label - text of button label
     - action - relative/absolute URL for action
    """

    return input(type="submit", class_="textbutton", alt=label, value=label)
</t>
<t tx="T289">def usage(msg='', exitcode=0):

    iswindows = (sys.platform == 'win32')
    if msg:
        print msg
    print "Options:"
    print "  -h, -?, --help       Display this help"
    print "  -v, --version        Display Freemail program version number"
    print "  -l, --logfile        File to write logging messages to"
    print "  -q, --quiet          Don't display log messages to stdout, only write to"
    print "                       logfile (default is both stdout and logfile)"
    if not iswindows:
        print "  -f, --foreground Run in foreground (default is to run in background)"
    print "  -i, --interactive    Run server in an interactive console - very handy"
    print "      --nostart        Used with '-i'. Don't launch the server yet"
    print "      --prompt         Sets prompt when running interactively"
    print "  -n, --node-address=  Hostname/IP addr of Freenet node, default localhost"
    print "  -p, --node-port=     FCP port number on this node, default 8481"
    print "  -P, --pop-port=      Port for POP3 server to listen on, default 10110"
    print "  -S, --smtp-port=     Port for SMTP server to listen on, default 10025"
    print "  -H, --http-port=     Port for HTTP server to listen on, default 8889"
    print "  -T, --telnet-port=   Port for Telnet server to listen on, default 10023"
    print "  -D, --database=      Path of database file, defaults to 'freemail.dat'"
    print "      --store-dir=     Path of store directory, ddefaults to 'freemail.store'"
    print "  -V, --verbosity=     Sets the verbosity of log output. Valid values are:"
    print "                       0=shutup, 1=critical, 2=normal, 3=trivial, 4=debug, 5=noisy"
    print "   --freenetverbosity  sets verbosity of freenet log messages - same scale"
    print "  -L, --listen-hosts=  comma-separated list of hosts from which to accept"
    print "                       POP3, SMTP and HTTP connections. Connecting from"
    print "                       any host not in this list results in immediate"
    print "                       disconnect. Defaults to '127.0.0.1'"
    print "  -t, --max-threads=   Sets a limit on the number of 'worker threads', the"
    print "                       threads that perform actual mail transfer. More"
    print "                       threads mean your email gets transferred faster, but"
    print "                       consumes more system resources. Recommend 3-10"
    if exitcode != None:
        sys.exit(exitcode)




</t>
<t tx="T290">def help():
    usage("", None)

</t>
<t tx="T291">def main(*args):

    import getopt

    global iswindows, m, db, conf
    iswindows = (sys.platform == 'win32')

    #print "sys.argv: %s" % str(sys.argv)
    try:
        opts, args = getopt.getopt(args,
                                   "h?vqifl:n:p:P:S:H:D:V:L:T:",
                                   ['help', 'version',
                                    'logfile=', 'foreground', 'interactive', 'nostart', 'prompt=',
                                    'node-address=', 'node-port=',
                                    'pop3-port=', 'smtp-port=', 'http-port=', 'telnet-port=',
                                    'database=', 'store-dir=',
                                    'verbosity=', 'freenetverbosity=', 'quiet',
                                    'listen-hosts=', 'max-threads='])
    except:
        import freemail
        import traceback
        traceback.print_exc(file=sys.stdout)
        freemail.usage("You entered an invalid option", 1)

    # set defaults
    showHelp = False
    showVersion = False
    nodeAddress = "127.0.0.1"
    nodePortStr = '8481'
    pop3PortStr = '10110'
    smtpPortStr = '10025'
    httpPortStr = '8889'
    telnetPortStr = '10023'
    database = "freemail.dat"
    storeDir = 'freemail.store'
    if iswindows:
        verbosityStr = '4'
        freenetVerbosityStr = '2'
    else:
        verbosityStr = '4'
        freenetVerbosityStr = '2'
    listenHostsStr = "127.0.0.1"
    maxThreadsStr = '5'
    runInForeground = iswindows
    logFile = "freemail.log"
    isQuiet = 0
    isInteractive = 0
    noStart = 0
    global interactivePrompt

    # grab raw options
    for opt, val in opts:
        if opt in ['-h', '-?', '--help']:
            showHelp = True
        elif opt in ['-v', '--version']:
            showVersion = True
        elif opt in ['-f', '--foreground'] and not iswindows:
            runInForeground = True
        elif opt in ['-l', '--logfile']:
            logFile = val
        elif opt in ['-q', '--quiet']:
            isQuiet = 1
        elif opt in ['-n', '--node-address']:
            nodeAddress = val
        elif opt in ['-p', '--node-port']:
            nodePortStr = val
        elif opt in ['-P', '--pop-port']:
            pop3PortStr = val
        elif opt in ['-H', '--http-port']:
            httpPortStr = val
        elif opt in ['-T', '--telnet-port']:
            telnetPortStr = val
        elif opt in ['-D', '--database']:
            database = val
        elif opt in ['--store-dir']:
            storeDir = val
        elif opt in ['-V', '--verbosity']:
            verbosityStr = val
        elif opt in ['--freenetverbosity']:
            freenetVerbosityStr = val
        elif opt in ['-L', '--listen-hosts']:
            listenHostsStr = val
        elif opt in ['-t', '--max-threads']:
            maxThreadsStr = val
        elif opt in ['-i', '--interactive']:
            isInteractive = 1
            runInForeground = 1
        elif opt in ['--nostart']:
            isInteractive = 1
            runInForeground = 1
            noStart = 1
        elif opt in ['--prompt']:
            interactivePrompt = val

    # intercept terminal options
    if showHelp:
        usage("Freemail version %s" % version, 0)
    if showVersion:
        print "Freemail version %s" % version
        sys.exit(0)

    # convert numerical arguments
    try:
        nodePort = int(nodePortStr)
    except:
        usage("Bad Freenet node port '%s'" % nodePortStr, 1)
    try:
        pop3Port = int(pop3PortStr)
    except:
        usage("Bad POP3 server port '%s'" % pop3PortStr, 1)
    try:
        smtpPort = int(smtpPortStr)
    except:
        usage("Bad SMTP server port '%s'" % smtpPortStr, 1)
    try:
        httpPort = int(httpPortStr)
    except:
        usage("Bad HTTP server port '%s'" % httpPortStr, 1)
    try:
        telnetPort = int(telnetPortStr)
    except:
        usage("Bad Telnet server port '%s'" % telnetPortStr, 1)
    try:
        verbosityLevel = int(verbosityStr)
    except:
        usage("Bad verbosity value '%s'" % verbosityStr, 1)
    try:
        freenetVerbosityLevel = int(freenetVerbosityStr)
    except:
        usage("Bad freenet verbosity value '%s'" % freenetVerbosityStr, 1)
    try:
        maxThreads = int(maxThreadsStr)
    except:
        usage("Bad maximum thread count '%s'" % maxThreadsStr, 1)

    #print opts

    # get listen hosts list
    try:
        listenHosts = [s.strip() for s in listenHostsStr.split(",")]
    except:
        usage("Bad listen hosts list '%s'" % listenHostsStr, 1)

    if 0:
        print "nodeAddress =     %s" % nodeAddress
        print "nodePort =        %s" % nodePort
        print "popPort =         %s" % pop3Port
        print "smtpPort =        %s" % smtpPort
        print "httpPort =        %s" % httpPort
        print "database =        %s" % database
        print "verbosity =       %s" % verbosityLevel
        print "listenHosts =     %s" % str(listenHosts)
        print "maxThreads =      %s" % maxThreads
        print "logFile =         %s" % logFile
        print "isQuiet =         %s" % isQuiet
        if not iswindows:
            print "runInForeground = %s" % runInForeground

    if not runInForeground:
        args = " ".join(sys.argv[1:])
        os.system("./freemail.py -f %s &amp;" % args)
        sys.exit(0)

    # running in foreground - write out a pid
    fd = open("freemail.pid", "w")
    fd.write("%s" % os.getpid())
    fd.close()

    # Create a freemail server object
    m = freemailServer(
        popHosts=listenHosts,
        smtpHosts=listenHosts,
        httpHosts=listenHosts,
        telnetHosts=listenHosts,
        fcpHost=nodeAddress,
        fcpPort=nodePort,
        popPort=pop3Port,
        smtpPort=smtpPort,
        httpPort=httpPort,
        telnetPort=telnetPort,
        database=database,
        storeDir=storeDir,
        maxWorkerThreads=maxThreads,
        logfile=logFile,
        verbosity=verbosityLevel,
        freenetverbosity=freenetVerbosityLevel,
        quiet=isQuiet,
        )

    # set logging verbosity
    m.verbosity(verbosityLevel)

    # launch the shell if interactive
    if isInteractive:
        db = m.db
        conf = db.config
        if not noStart:
            m.startServer()
        con = freemailConsole(globals())
        con.run()
    else:
        # and run it
        m.runServer()
</t>
<t tx="T292">if __name__ == '__main__':
    main(*(sys.argv[1:]))
</t>
<t tx="T293"></t>
<t tx="T294">@first #!/bin/bash

# start-freemail.sh

if [[ -f freemail.pid ]]
then
  echo "FreeMail is already running - please run stop-freemail.sh first"
  echo
  echo "(if you're absolutely sure FreeMail is not running, you should"
  echo "delete the freemail.pid file and try again)."
  echo
else
  python freemail.py $*
fi
</t>
<t tx="T295">@first #!/bin/bash

# terminates FreeMail

if [[ -f freemail.pid ]]
then
 kill -9 `cat freemail.pid`
 rm freemail.pid
 echo "FreeMail terminated"
else
 echo "FreeMail is not running"
fi

</t>
<t tx="T296">@first #!/usr/bin/python

"""
Launcher that runs FreeMail within a python shell
"""

@others
</t>
<t tx="T297">import code
try:
    import readline
except:
    pass

import sys, os
from freemail import freemailServer, cell, slotmap, build
import freemail

</t>
<t tx="T298">def v(n):
    global m
    m.verbosity(n)

if len(sys.argv) &gt; 1:
    if sys.argv[1] in ['-h', '--help', '-help', '-?']:
        print "Usage: %s [datafile]" % sys.argv[0]
        print "default datafile is 'freemail.dat'"
        sys.exit(0)
    else:
        datafile = sys.argv[1]
else:
    datafile = "freemail.dat"

m = freemailServer(database=datafile)
db = m.db
conf = db.config
m.startServer()

con = freemailConsole(locals())
con.run()
</t>
<t tx="T299"># Makefile for SSLCrypto python extension

# if the make screws up, you might need to edit the 2 variables
# at the top of the file 'setup.py'.

SSLCrypto: SSLCrypto.so

SSLCrypto.so: src/SSLCrypto.c src/die.c
	python setup.py build_ext --inplace

clean:
	rm -rf SSLCrypto.so src/SSLCrypto.o src/die.o build

doco:
	epydoc -n "FreeMail API" -o doc/classes freemail.py
</t>
<t tx="T300">Welcome to the maiden release of FreeMail!

---------------------
* What is 'FreeMail'?

Freemail is a Mail Transfer Agent (MTA), or Mailserver, that supports you
in enjoying an unprecedented level of privacy and anonymity in sending
and receiving email messages.

It uses strong encryption and powerful anonymity features to make it nigh
impossible for an attacker to determine the contents of your email messages,
who you're sending to or when.

----------------------
* Freemail features

   * disguises your email addresses
   * avoids detection of who you're sending email to or receiving from
   * makes it almost impossible to detect even *if* or when you're sending or
     receiving messages
   * works 100% peer to peer
   * uses the Freenet network for high-grade privacy protection
   * totally compatible with your email client program, since it runs

----------------------------------
* How does FreeMail compare to PGP?

It's way more secure. No comparison.

With PGP, an attacker can still determine:
 * who you are sending email messages to and receiving messages from
 * when you are sending and receiving these messages
 * the subject lines of these messages

With FreeMail, all this information is hidden.

----------------------------------------
* How does FreeMail compare to Hushmail?

Arguably much better.

With Hushmail, it is possible for an attacker to detect that you're
sending or receiving Hushmail messages, and when. This alone can
give a lot of information in many cases.

Also, Hushmail is only as good as the operators of the Hushmail
service. Hushmail works with 'managed keys', which means that your
private encryption keys get stored as plaintext on the Hushmail
servers. It is not inconceivable that even now, attackers (eg 
government agencies) have gotten access to these keys. Which takes
away much of the very reason for using Hushmail in the first place.

With Freemail, your decryption keys are stored only your own hard disk.
You can even store them on an encrypted virtual disk (using PGPDisk or
BestCrypt etc).

Also, since Freemail uses Freenet as its transport, it becomes almost
impossible to determine even *if* you're sending or receiving messages.

--------------------------------------------
* What do I need to run FreeMail?

Requirements are:

 * A graphical web browser
 * A standard POP/SMTP-capable email client program, like Mozilla,
   Eudora, KMail, Evolution, Pine, Mutt or (if you must) Outlook (Express).
 * Freenet software installed and running (or, access to a Freenet FCP
   connection on another system). See www.freenetproject.org
 * Linux, BSD or an insecure, proprietary Microsoft operating system
 * If running Linux or BSD, you'll also need:
    * Python 2.2 or later installed
    * glibc 2.3 or later

-------------------------------------
* How do I run FreeMail?

If you're running on Linux or BSD, type:

  ./start-freemail.sh -h

to see the available options.

Once you've chosen your options, then launch the FreeMail program.

If you're running FreeMail on one box, but want to access it with an
email client or web browser on another host, then the first time you
run it, you'll have to specify the '--listen-hosts' argument. From then
on, the listen hosts will be saved in database so you won't have to
specify this again.

When you first run FreeMail, you'll see a message giving an address
to point your web browser, so you can complete the initial configuration
process.

------------------------------
* How do I configure FreeMail?

FreeMail has a web-based administrative interface.

By default, this interface can be reached by pointing your web browser
to port 8889.

For example, if you're running FreeMail on your local machine, then
point your web browser to http://127.0.0.1:8889


--------------------------------
* Can I extend FreeMail?

Sure. If you're adept with Python, then look at the freemail.py
source file, and study the freemailServer class.

By deriving from this class, and providing some method overrides,
you can get FreeMail to do anything that turns you on.

If you're opening up the .leo file, please first set the tabs in your
preferences to -4. Otherwise, Leo will munge up the indentation and trash
your py files.


--------------------------------
* Where can I get help?
</t>
<t tx="T301">@
Quick installation instructions for FreeMail.

--------------------------------------------
** If you have downloaded the generic version:

You'll need to build the python SSLCrypto extension module.

For this, you'll need the OpenSSL and Python development packages
(headers plus libs) installed on your system.

To build the SSLCrypto extension, simply type 'make'.

If the make screws up, h[ave a look around your system and find out
where the OpenSSL 'crypto' library is installed. For example, on
Linux systems, it's usually in /usr/lib/libcrypto.so or
/usr/local/lib/libcrypto.so. Ditto for the OpenSSL include files,
which usually live in /usr/include, or /usr/local/include.
When you've determined these directories, edit the settings in the
first couple of lines of the 'setup.py' file. When you get these
settings right, the OpenSSL extension will build just fine.

Note that the file src/SSLCrypto.c is generated by the Pyrex Python/C
wrapper generator, from the source file src/SSLCrypto.pyx. If you want
to vet the source and build your own src/SSLCrypto.c, you'll need to
install the Pyrex compiler, which you can get from the Pyrex website
at: http://www.cosc.canterbury.ac.nz/~greg/python/Pyrex/

------------------------------------------------------------------------
** If you have downloaded the source version with windows binary module

you should see the file SSLCrypto.pyd
in this directory, in which case FreeMail should run according
to the instructions in the README file


------------------------------------------------------------------
** If you have downloaded the source version with Linux/BSD binary module

you should see the file SSLCrypto.so
in this top-level directory, in which case FreeMail **MAY** work
out of the box - refer to README.

If it crashes or locks up in strange places, (which will happen on some
linux distros like Gentoo), you will instead need to download the generic
version and build the binary module.

------------------------------------------------------------------

Any problems with building - please contact david@freenet.org.nz
</t>
<t tx="T302">GNU GENERAL PUBLIC LICENSE
Version 2, June 1991

Copyright (C) 1989, 1991 Free Software Foundation, Inc.
59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Everyone is permitted to copy and distribute verbatim copies
of this license document, but changing it is not allowed.

Preamble

The licenses for most software are designed to take away your
freedom to share and change it.  By contrast, the GNU General Public
License is intended to guarantee your freedom to share and change free
software--to make sure the software is free for all its users.  This
General Public License applies to most of the Free Software
Foundation's software and to any other program whose authors commit to
using it.  (Some other Free Software Foundation software is covered by
the GNU Library General Public License instead.)  You can apply it to
your programs, too.

  When we speak of free software, we are referring to freedom, not
price.  Our General Public Licenses are designed to make sure that you
have the freedom to distribute copies of free software (and charge for
this service if you wish), that you receive source code or can get it
if you want it, that you can change the software or use pieces of it
in new free programs; and that you know you can do these things.

  To protect your rights, we need to make restrictions that forbid
anyone to deny you these rights or to ask you to surrender the rights.
These restrictions translate to certain responsibilities for you if you
distribute copies of the software, or if you modify it.

  For example, if you distribute copies of such a program, whether
gratis or for a fee, you must give the recipients all the rights that
you have.  You must make sure that they, too, receive or can get the
source code.  And you must show them these terms so they know their
rights.

  We protect your rights with two steps: (1) copyright the software, and
(2) offer you this license which gives you legal permission to copy,
distribute and/or modify the software.

  Also, for each author's protection and ours, we want to make certain
that everyone understands that there is no warranty for this free
software.  If the software is modified by someone else and passed on, we
want its recipients to know that what they have is not the original, so
that any problems introduced by others will not reflect on the original
authors' reputations.

  Finally, any free program is threatened constantly by software
patents.  We wish to avoid the danger that redistributors of a free
program will individually obtain patent licenses, in effect making the
program proprietary.  To prevent this, we have made it clear that any
patent must be licensed for everyone's free use or not licensed at all.

  The precise terms and conditions for copying, distribution and
modification follow.

GNU GENERAL PUBLIC LICENSE
TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. This License applies to any program or other work which contains
a notice placed by the copyright holder saying it may be distributed
under the terms of this General Public License.  The "Program", below,
refers to any such program or work, and a "work based on the Program"
means either the Program or any derivative work under copyright law:
that is to say, a work containing the Program or a portion of it,
either verbatim or with modifications and/or translated into another
language.  (Hereinafter, translation is included without limitation in
the term "modification".)  Each licensee is addressed as "you".

Activities other than copying, distribution and modification are not
covered by this License; they are outside its scope.  The act of
running the Program is not restricted, and the output from the Program
is covered only if its contents constitute a work based on the
Program (independent of having been made by running the Program).
Whether that is true depends on what the Program does.

  1. You may copy and distribute verbatim copies of the Program's
source code as you receive it, in any medium, provided that you
conspicuously and appropriately publish on each copy an appropriate
copyright notice and disclaimer of warranty; keep intact all the
notices that refer to this License and to the absence of any warranty;
and give any other recipients of the Program a copy of this License
along with the Program.

You may charge a fee for the physical act of transferring a copy, and
you may at your option offer warranty protection in exchange for a fee.

  2. You may modify your copy or copies of the Program or any portion
of it, thus forming a work based on the Program, and copy and
distribute such modifications or work under the terms of Section 1
above, provided that you also meet all of these conditions:

    a) You must cause the modified files to carry prominent notices
    stating that you changed the files and the date of any change.

    b) You must cause any work that you distribute or publish, that in
    whole or in part contains or is derived from the Program or any
    part thereof, to be licensed as a whole at no charge to all third
    parties under the terms of this License.

    c) If the modified program normally reads commands interactively
    when run, you must cause it, when started running for such
    interactive use in the most ordinary way, to print or display an
    announcement including an appropriate copyright notice and a
    notice that there is no warranty (or else, saying that you provide
    a warranty) and that users may redistribute the program under
    these conditions, and telling the user how to view a copy of this
    License.  (Exception: if the Program itself is interactive but
    does not normally print such an announcement, your work based on
    the Program is not required to print an announcement.)

These requirements apply to the modified work as a whole.  If
identifiable sections of that work are not derived from the Program,
and can be reasonably considered independent and separate works in
themselves, then this License, and its terms, do not apply to those
sections when you distribute them as separate works.  But when you
distribute the same sections as part of a whole which is a work based
on the Program, the distribution of the whole must be on the terms of
this License, whose permissions for other licensees extend to the
entire whole, and thus to each and every part regardless of who wrote it.

Thus, it is not the intent of this section to claim rights or contest
your rights to work written entirely by you; rather, the intent is to
exercise the right to control the distribution of derivative or
collective works based on the Program.

In addition, mere aggregation of another work not based on the Program
with the Program (or with a work based on the Program) on a volume of
a storage or distribution medium does not bring the other work under
the scope of this License.

  3. You may copy and distribute the Program (or a work based on it,
under Section 2) in object code or executable form under the terms of
Sections 1 and 2 above provided that you also do one of the following:

    a) Accompany it with the complete corresponding machine-readable
    source code, which must be distributed under the terms of Sections
    1 and 2 above on a medium customarily used for software interchange; or,

    b) Accompany it with a written offer, valid for at least three
    years, to give any third party, for a charge no more than your
    cost of physically performing source distribution, a complete
    machine-readable copy of the corresponding source code, to be
    distributed under the terms of Sections 1 and 2 above on a medium
    customarily used for software interchange; or,

    c) Accompany it with the information you received as to the offer
    to distribute corresponding source code.  (This alternative is
    allowed only for noncommercial distribution and only if you
    received the program in object code or executable form with such
    an offer, in accord with Subsection b above.)

The source code for a work means the preferred form of the work for
making modifications to it.  For an executable work, complete source
code means all the source code for all modules it contains, plus any
associated interface definition files, plus the scripts used to
control compilation and installation of the executable.  However, as a
special exception, the source code distributed need not include
anything that is normally distributed (in either source or binary
form) with the major components (compiler, kernel, and so on) of the
operating system on which the executable runs, unless that component
itself accompanies the executable.

If distribution of executable or object code is made by offering
access to copy from a designated place, then offering equivalent
access to copy the source code from the same place counts as
distribution of the source code, even though third parties are not
compelled to copy the source along with the object code.

  4. You may not copy, modify, sublicense, or distribute the Program
except as expressly provided under this License.  Any attempt
otherwise to copy, modify, sublicense or distribute the Program is
void, and will automatically terminate your rights under this License.
However, parties who have received copies, or rights, from you under
this License will not have their licenses terminated so long as such
parties remain in full compliance.

  5. You are not required to accept this License, since you have not
signed it.  However, nothing else grants you permission to modify or
distribute the Program or its derivative works.  These actions are
prohibited by law if you do not accept this License.  Therefore, by
modifying or distributing the Program (or any work based on the
Program), you indicate your acceptance of this License to do so, and
all its terms and conditions for copying, distributing or modifying
the Program or works based on it.

  6. Each time you redistribute the Program (or any work based on the
Program), the recipient automatically receives a license from the
original licensor to copy, distribute or modify the Program subject to
these terms and conditions.  You may not impose any further
restrictions on the recipients' exercise of the rights granted herein.
You are not responsible for enforcing compliance by third parties to
this License.

  7. If, as a consequence of a court judgment or allegation of patent
infringement or for any other reason (not limited to patent issues),
conditions are imposed on you (whether by court order, agreement or
otherwise) that contradict the conditions of this License, they do not
excuse you from the conditions of this License.  If you cannot
distribute so as to satisfy simultaneously your obligations under this
License and any other pertinent obligations, then as a consequence you
may not distribute the Program at all.  For example, if a patent
license would not permit royalty-free redistribution of the Program by
all those who receive copies directly or indirectly through you, then
the only way you could satisfy both it and this License would be to
refrain entirely from distribution of the Program.

If any portion of this section is held invalid or unenforceable under
any particular circumstance, the balance of the section is intended to
apply and the section as a whole is intended to apply in other
circumstances.

It is not the purpose of this section to induce you to infringe any
patents or other property right claims or to contest validity of any
such claims; this section has the sole purpose of protecting the
integrity of the free software distribution system, which is
implemented by public license practices.  Many people have made
generous contributions to the wide range of software distributed
through that system in reliance on consistent application of that
system; it is up to the author/donor to decide if he or she is willing
to distribute software through any other system and a licensee cannot
impose that choice.

This section is intended to make thoroughly clear what is believed to
be a consequence of the rest of this License.

  8. If the distribution and/or use of the Program is restricted in
certain countries either by patents or by copyrighted interfaces, the
original copyright holder who places the Program under this License
may add an explicit geographical distribution limitation excluding
those countries, so that distribution is permitted only in or among
countries not thus excluded.  In such case, this License incorporates
the limitation as if written in the body of this License.

  9. The Free Software Foundation may publish revised and/or new versions
of the General Public License from time to time.  Such new versions will
be similar in spirit to the present version, but may differ in detail to
address new problems or concerns.

Each version is given a distinguishing version number.  If the Program
specifies a version number of this License which applies to it and "any
later version", you have the option of following the terms and conditions
either of that version or of any later version published by the Free
Software Foundation.  If the Program does not specify a version number of
this License, you may choose any version ever published by the Free Software
Foundation.

  10. If you wish to incorporate parts of the Program into other free
programs whose distribution conditions are different, write to the author
to ask for permission.  For software which is copyrighted by the Free
Software Foundation, write to the Free Software Foundation; we sometimes
make exceptions for this.  Our decision will be guided by the two goals
of preserving the free status of all derivatives of our free software and
of promoting the sharing and reuse of software generally.

			    NO WARRANTY

  11. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY
FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN
OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED
OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS
TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE
PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,
REPAIR OR CORRECTION.

  12. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,
INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING
OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED
TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY
YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER
PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE
POSSIBILITY OF SUCH DAMAGES.

		     END OF TERMS AND CONDITIONS

	    How to Apply These Terms to Your New Programs

  If you develop a new program, and you want it to be of the greatest
possible use to the public, the best way to achieve this is to make it
free software which everyone can redistribute and change under these terms.

  To do so, attach the following notices to the program.  It is safest
to attach them to the start of each source file to most effectively
convey the exclusion of warranty; and each file should have at least
the "copyright" line and a pointer to where the full notice is found.

    &lt;one line to give the program's name and a brief idea of what it does.&gt;
    Copyright (C) 19yy  &lt;name of author&gt;

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


Also add information on how to contact you by electronic and paper mail.

If the program is interactive, make it output a short notice like this
when it starts in an interactive mode:

    Gnomovision version 69, Copyright (C) 19yy name of author
    Gnomovision comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
    This is free software, and you are welcome to redistribute it
    under certain conditions; type `show c' for details.

The hypothetical commands `show w' and `show c' should show the appropriate
parts of the General Public License.  Of course, the commands you use may
be called something other than `show w' and `show c'; they could even be
mouse-clicks or menu items--whatever suits your program.

You should also get your employer (if you work as a programmer) or your
school, if any, to sign a "copyright disclaimer" for the program, if
necessary.  Here is a sample; alter the names:

  Yoyodyne, Inc., hereby disclaims all copyright interest in the program
  `Gnomovision' (which makes passes at compilers) written by James Hacker.

  &lt;signature of Ty Coon&gt;, 1 April 1989
  Ty Coon, President of Vice

This General Public License does not permit incorporating your program into
proprietary programs.  If your program is a subroutine library, you may
consider it more useful to permit linking proprietary applications with the
library.  If this is what you want to do, use the GNU Library General
Public License instead of this License.
</t>
<t tx="T303">@
Log of changes to FreeMail
--------------------------

Build 020:

    * Tuned up peersite retrieval algorithm
    * Incorporated updated pyFreenet module (v0.2.5)


Build 019:

    * Fixed critical bug in POP3 server


Build 018:

    * All mail identities now insert hourly, even though the DBR period is still
      24 hours - this aims to help keep identities more persistent


Build 017:

    * Added missing __delitem__ method to cell class
    * fixed cell.__delitem__ to handle integer or key args


Build 016:

    * Hacked SMTP server to compensate for pine's weird command sequence
      (sending RSET after the HELO)

    * Changed insert protocol to speed up mail transport, and make it more reliable.
      This involved:
       - Changing DBR period for inserting identity records, from 3 days to 1 day
       - Changing the identity record fetch routine to more quickly get the latest
         available version of a peer's mailsite. This is done by launching several
         threads, each of which tries to retrieve the peer's mailsite n periods back.

    * With this change, you (or your peers) can run FreeMail as seldom as once each
      two weeks, and mail will still get through (although running daily is recommended).
    * enabled multi-threading in FEC inserts in freenet.py

    * removed fatal FEC retrieval bug

    * removed 'Manage Peers' button on webui that was crashing things

    * added display of SSK public/private keys to identity display page


Build 015:

    * Fixed windows exe installer (broken since build 011)
      (stopped using McMillan Installer, switched to Py2EXE)

    * Added '-i' startup option to allow interactive console

    * Added a telnet server

    * Added more rigorous exception handling

    * Added more bounce messages in cases where message delivery fails


Build 014:

    * Added handlers to fix missing 'retryBackoff' attribute in messages
    * Added code to log cell attribute exceptions to file 'freemail.log.crash'


Build 013:

    * Fixed __iter__ attribute error (which was hitting python2.x users)

    * Modified setup.py script to build SSLCrypto, and to work whether
      the Pyrex compiler is installed or not. This overcomes the build
      problems which were affecting some users (particularly Mac OSX)


Build 012:

    * Implemented exponential backoff/retry for re-sends of
      unreceipted messages

    * Added code to attempt transparent recovery from
      Freenet/Entropy node outages

    * Rebuilt the SSLCrypto binary (and C source/header) under
      Python 2.2.1, which should eliminate problems for people with
      2.2.1 and 2.2.2

    * Added code to check python version and complain if python is too old

    * Added automated database format upgrades, so hopefully you will never
      need to wipe your database again. The update code will be maintained
      to convert databases from all builds from 011 onwards.


Build 011:

    * MANDATORY UPGRADE - INCOMPATIBLE WITH EARLIER RELEASES

    * Ditch your old database and store directory please

    * Changed freemail address format from 'fred@blahblahPAgM'
      to 'fred@blahblah.freemail'

    * Totally overhauled the in-freenet protocol:
        * eliminated all RTS/CTS phase (too cumbersome)
        * Replaced 'per-relationship' SSK queues with 'per-account'
          KSK queues - protected against spamming/spoofing via
          signature by sender and encryption to recipient. No way of
          spoofing messages unless you can compromise an SSK key
        * eliminated scheduled retries of past empty queue slots; now
          relying fully on the send retry mechanism

    * Implemented optional POPmail receipts for all successfully
      delivered messages (can turn off in config)

    * Implemented bounces to POP inboxes for messages that couldn't
      be sent due to freenet exceptions (eg RouteNotFound errors)

    * message queues are polled via a 'sliding window' mechanism, which
      should, together with the retry mechanism, prove sufficiently robust

    * provided an interactive FreeMail console - script 'console.py', which
      launches a FreeMail server, and gives you a Python console for inspecting
      and manipulating the server objects


Build 010:

    * Eliminated all use of Metakit, due to complications suffered by
      users of some OSs in getting it built, importing it etc. This eliminates
      a whole class of problems

    * Added a generic data storage class called 'cell', which behaves kinda like
      a cross between a sequence and a dict, and also contains some of the methods
      of the old metakitplus module.

    * database is now saved as a python pickle

-----

Sorry there are no change notes for earlier versions

</t>
<t tx="T304">@
This file lists some of the intended additions for future versions of FreeMail

It also serves as a 'todo' list for me the author

* Mailing lists - 2 modes:
    - secure:
        - moderated
        - mailing list account receives posts, and re-mails them to all
          subscribers
        - easy to drop miscreants from the list - just take them off the re-send list
        - very slow and non-scalable
    - insecure
        - issue private key for mail identity to everyone
        - all subscribers poll the same KSK queue
        - very fast and 100% scalable
        - difficult to kick people off the list

* Eventual compulsory ElGamal keys
    - existing RSA keys not as secure as hoped (nop has informed me there's a one-step
      fast cracking operation
    - phase in compulsory ElGamal keys - much more secure

* fix problems in web interface seen with some windows installations and IE

* Test with very large files - ensure the FEC logic is working OK

</t>
<t tx="T305"></t>
<t tx="T306">import freenet
from freemail import *
import SSLCrypto
import smtplib
import time

freenet.verbosity(4)

import os, sys

isWindows = (sys.platform == 'win32')

try:
    os.unlink("freemail.dat")
except:
    pass

# -------------------------------------
# real live tests:
# - create two identities 'fred' and 'mary'
# - enqueue a message from 'fred' to 'mary'
# - see if:
#    - red RTSs mary
#    - mary CTSs fred
#    - fred sends message to mary
#    - mary gets message and delivers to POP3

# create a mailserver

m = freemailServer(fcpHost='hermes',
                   fcpPort=8482,
                   popPort=10110,
                   smtpPort=10025,
                   httpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   popHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   smtpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   configState='ready',
                   #configState='new',
                   adminPassword="freemail",
                   htlSend=0,
                   htlReceive=0,
                   maxWorkerThreads=7,
                   slotLookAhead=1,
                   logfile=sys.stdout,
                   )
m.verbosity(4)

# create identities

freenet.verbosity(2)

#fredAddr = m.dbAddIdentity('fred', password='blah')
#maryAddr = m.dbAddIdentity('mary', password='blah')

def tx():

    # create a message
    msg = """Subject: Freemail test fred-&gt;mary

Hi Mary,
    
This is a message.
Hope you get it!

Cheers,
Fred
"""
    fromHdr = "From: %s\r\n" % fredAddr
    toHdr = "To: %s\r\n" % maryAddr
    date = "Date: %s\r\n" % time.asctime(time.localtime(time.time()))
    msg = fromHdr + toHdr + date + msg
    
    s = smtplib.SMTP("localhost", 10025)
    #s.sendmail(fredAddr, [maryAddr], msg)
    s.sendmail(fredAddr, [fredAddr], msg)

def rx():
    import poplib
    
    p = poplib.POP3("localhost", 10110)
    #p.user(maryAddr)
    p.user(fredAddr)
    p.pass_("blah")
    p.list()

    numMsgs, totalSize = p.stat()
    if numMsgs == 0:
        print "*** NO MESSAGES ***"
    else:
        for i in range(1, numMsgs + 1):
            (header, msg, octets) = p.retr(i)
            print "Message ", `i`, ':'
            for line in msg:
                print '   ' + line
            print '-----------------------'
        for i in range(1, numMsgs+1):
            p.dele(i)
    p.quit()


addrFred = m.dbAddIdentity('fred', password='blah')
addrMary = m.dbAddIdentity('mary', password='blah')

idFred = m.db.identities[addrFred]
idMary = m.db.identities[addrMary]

keyFred = SSLCrypto.key(idFred.idCryptoKey)
keyMary = SSLCrypto.key(idMary.idCryptoKey)

def txrx():
    # send some shit from fred to mary
    k = m.fcpTransmitMessage(idAddr=addrFred,
                             peerAddr=addrMary,
                             type='message',
                             body="Hi Mary,\n\nThis is Fred.\n\nHope you receive this!\n\nCheers\nFred\n",
                             )

    # ct = node.get(k)
    # pt = m.decryptAndVerify(ct, keyFred, keyMary)
    # msg = m.fcpParseMailMsg(pt)

    # msg = m.fcpGetMsgFromSlot(addrMary, 0)
    # return msg

#txrx()

m.startServer()

#time.sleep(5)

#m.fcpRefreshMailsite(addrFred, htl=0, future=0)
#m.fcpRefreshMailsite(addrMary, htl=0, future=0)

node = freenet.node("hermes", 8482)

#d = m.fcpGetPeerInfo(fredAddr, retryWait=1)

</t>
<t tx="T307">@first #!/usr/bin/python -i

import freenet
from freemail import *
import SSLCrypto
import smtplib
import time

freenet.verbosity(4)

import os, sys

isWindows = (sys.platform == 'win32')

if len(sys.argv) &gt; 1 and sys.argv[1] == 'cont':
    cont = 1
    print "CONTINUING TEST"
else:
    cont = 0

fcpHost = "hermes"
fcpPort = 8482


# -------------------------------------
# real live tests:
# - create two identities 'fred' and 'mary'
# - enqueue a message from 'fred' to 'mary'
# - see if:
#    - red RTSs mary
#    - mary CTSs fred
#    - fred sends message to mary
#    - mary gets message and delivers to POP3

# create a mailserver

if isWindows:
    # windows
    storeDir = 'freemail-w2k.store'
    dataFile = 'freemail-w2k.db'
    logFileName = 'freemail-w2k.log'
    if cont:
        print "Continuing windows test run"
    else:
        print "new windows test run"
        if os.path.isfile(dataFile):
            os.unlink(dataFile)
        os.system("del %s /q /s" % storeDir)

else:
    # proper operating systems
    storeDir = 'freemail.store'
    dataFile = 'freemail.dat'
    logFileName = 'freemail.log'
    if not cont:
        if os.path.isfile(dataFile):
            os.unlink(dataFile)
        os.system("rm -rf %s" % storeDir)

m = freemailServer(fcpHost=fcpHost,
                   fcpPort=fcpPort,
                   popPort=10110,
                   smtpPort=10025,
                   httpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   popHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   smtpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   configState='ready',
                   htlSend=0,
                   htlReceive=0,
                   maxWorkerThreads=7,
                   slotLookAhead=1,
                   logFile=logFileName,
                   database=dataFile,
                   storeDir=storeDir,
                   verbosity=4,
                   # txMaxRetries=1,
                   )
m.verbosity(4)

# create identities

freenet.verbosity(2)

if 0 and isWindows:
    m.runServer()
else:
    print "Launching freemail server for test run"
    m.startServer()

time.sleep(5)

if not cont:
    if isWindows:
        fredAddr = m.dbAddIdentity('fred-win', password='blah')
        open("\\pywindows\\myprogs\\freemail\\addr-fred-win", "w").write(fredAddr)
        maryAddr = m.dbAddIdentity('mary-win', password='blah')
        open("\\pywindows\\myprogs\\freemail\\addr-mary-win", "w").write(maryAddr)
    else:
        fredAddr = m.dbAddIdentity('fred-nix', password='blah')
        open("/main/pywindows/myprogs/freemail/addr-fred-nix", "w").write(fredAddr)
        maryAddr = m.dbAddIdentity('mary-nix', password='blah')
        open("/main/pywindows/myprogs/freemail/addr-mary-nix", "w").write(maryAddr)

</t>
<t tx="T308">@first #!/usr/bin/python -i

"""
Variation of tst1.py which runs two separate freemail server instances
"""

import freenet
from freemail import *
import SSLCrypto
import smtplib
import time

freenet.verbosity(4)

import os, sys

isWindows = (sys.platform == 'win32')

if len(sys.argv) &gt; 1 and sys.argv[1] == 'cont':
    cont = 1
    print "CONTINUING TEST"
else:
    cont = 0

fcpHost = "hermes"
fcpPort = 8481

txMaxRetries = 4
txBackoffInit = 10
txBackoffMult = 1.5


# -------------------------------------
# real live tests:
# - create two identities 'fred' and 'mary'
# - enqueue a message from 'fred' to 'mary'
# - see if:
#    - red RTSs mary
#    - mary CTSs fred
#    - fred sends message to mary
#    - mary gets message and delivers to POP3

# create a mailserver

if isWindows:
    # windows
    storeDir1 = 'freemail-w2k.store-1'
    storeDir2 = 'freemail-w2k.store-2'
    dataFile1 = 'freemail-w2k.dat-1'
    dataFile2 = 'freemail-w2k.dat-2'
    logFileName1 = 'freemail-w2k.log-1'
    logFileName2 = 'freemail-w2k.log-2'
    if cont:
        print "Continuing windows test run"
    else:
        print "new windows test run"
        if os.path.isfile(dataFile1):
            os.unlink(dataFile1)
        os.system("del %s /q /s" % storeDir1)
        if os.path.isfile(dataFile2):
            os.unlink(dataFile2)
        os.system("del %s /q /s" % storeDir2)

else:
    # proper operating systems
    storeDir1 = 'freemail.store-1'
    storeDir2 = 'freemail.store-2'
    dataFile1 = 'freemail.dat-1'
    dataFile2 = 'freemail.dat-2'
    logFileName1 = 'freemail.log-1'
    logFileName2 = 'freemail.log-2'
    if not cont:
        if os.path.isfile(dataFile1):
            os.unlink(dataFile1)
        os.system("rm -rf %s" % storeDir1)
        if os.path.isfile(dataFile2):
            os.unlink(dataFile2)
        os.system("rm -rf %s" % storeDir2)

m1 = freemailServer(fcpHost=fcpHost,
                   fcpPort=fcpPort,
                   popPort=10110,
                   smtpPort=10025,
                   httpPort=8889,
                   telnetPort=10021,
                   httpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   popHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   smtpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   configState='ready',
                   htlSend=0,
                   htlReceive=0,
                   maxWorkerThreads=7,
                   slotLookAhead=1,
                   logFile=logFileName1,
                   database=dataFile1,
                   storeDir=storeDir1,
                   verbosity=4,
                   txMaxRetries=txMaxRetries,
                   txBackoffInit=txBackoffInit,
                   txBackoffMult=txBackoffMult,
                   )
m1.verbosity(4)

m2 = freemailServer(fcpHost=fcpHost,
                   fcpPort=fcpPort,
                   popPort=10111,
                   smtpPort=10026,
                   httpPort=8890,
                   telnetPort=10022,
                   httpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   popHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   smtpHosts=['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2'],
                   configState='ready',
                   htlSend=0,
                   htlReceive=0,
                   maxWorkerThreads=7,
                   slotLookAhead=1,
                   logFile=logFileName2,
                   database=dataFile2,
                   storeDir=storeDir2,
                   verbosity=4,
                   txMaxRetries=txMaxRetries,
                   txBackoffInit=txBackoffInit,
                   txBackoffMult=txBackoffMult,
                   )
m1.verbosity(4)


# create identities

freenet.verbosity(2)

print "Launching 2 freemail servers for test run"
m1.startServer()
m2.startServer()

time.sleep(5)

if not cont:
    if isWindows:
        fredAddr = m1.dbAddIdentity('fred-win', password='blah')
        open("\\py\\myprogs\\freemail\\addr-fred-win", "w").write(fredAddr)
        maryAddr = m2.dbAddIdentity('mary-win', password='blah')
        open("\\py\\myprogs\\freemail\\addr-mary-win", "w").write(maryAddr)
    else:
        fredAddr = m1.dbAddIdentity('fred-nix', password='blah')
        open("/main/pywindows/myprogs/freemail/addr-fred-nix", "w").write(fredAddr)
        maryAddr = m2.dbAddIdentity('mary-nix', password='blah')
        open("/main/pywindows/myprogs/freemail/addr-mary-nix", "w").write(maryAddr)

</t>
<t tx="T309">@first #!/usr/bin/python -i

"""
For testing the 'cell' class

"""

@others
</t>
<t tx="T310">def test1():
    x = cell()

    for a in range(0, 4):
        for b in range(0, 4):
            x.append(a=a, b=b)

    y = x.select(a=1)


def fmtest():

    import freemail

    f = freemail.freemailServer(verbosity=4)

    f.dbSave()

</t>
<t tx="T311">@others
</t>
<t tx="T312">@others
</t>
<t tx="T313"># Written by Petru Paler
# see LICENSE.txt for license information

from types import IntType, LongType, StringType, ListType, TupleType, DictType
import re
from cStringIO import StringIO
</t>
<t tx="T314">int_filter = re.compile('(0|-?[1-9][0-9]*)e')
string_filter = re.compile('(0|[1-9][0-9]*):')
</t>
<t tx="T315">def decode_int(x, f):
    m = int_filter.match(x, f)
    if m is None:
        raise ValueError
    return (long(m.group(1)), m.end())
</t>
<t tx="T316">def decode_string(x, f):
    m = string_filter.match(x, f)
    if m is None:
        raise ValueError
    l = int(m.group(1))
    s = m.end()
    return (x[s:s+l], s + l)
</t>
<t tx="T317">def decode_list(x, f):
    r = []
    while x[f] != 'e':
        v, f = bdecode_rec(x, f)
        r.append(v)
    return (r, f + 1)
</t>
<t tx="T318">def decode_dict(x, f):
    r = {}
    lastkey = None
    while x[f] != 'e':
        k, f = decode_string(x, f)
        if lastkey is not None and lastkey &gt;= k:
            raise ValueError
        lastkey = k
        v, f = bdecode_rec(x, f)
        r[k] = v
    return (r, f + 1)
</t>
<t tx="T319">def bdecode_rec(x, f):
    t = x[f]
    if t == 'i':
        return decode_int(x, f + 1)
    elif t == 'l':
        return decode_list(x, f + 1)
    elif t == 'd':
        return decode_dict(x, f + 1)
    else:
        return decode_string(x, f)
</t>
<t tx="T320">def bdecode(x):
    try:
        r, l = bdecode_rec(x, 0)
    except IndexError:
        raise ValueError
    if l != len(x):
        raise ValueError
    return r
</t>
<t tx="T321">def bencode_rec(x, b):
    t = type(x)
    if t in (IntType, LongType):
        b.write('i%de' % x)
    elif t is StringType:
        b.write('%d:%s' % (len(x), x))
    elif t in (ListType, TupleType):
        b.write('l')
        for e in x:
            bencode_rec(e, b)
        b.write('e')
    elif t is DictType:
        b.write('d')
        keylist = x.keys()
        keylist.sort()
        for k in keylist:
            assert type(k) is StringType
            bencode_rec(k, b)
            bencode_rec(x[k], b)
        b.write('e')
    else:
        assert 0
</t>
<t tx="T322">def bencode(x):
    b = StringIO()
    bencode_rec(x, b)
    return b.getvalue()
</t>
<t tx="T323">@first #!/usr/bin/python -i
@others
</t>
<t tx="T324">if __name__ == '__main__':

    s = slotmap()
    s[0] = 1
    s[1] = 0
    s[2] = 1
    s[3] = 1
    s[4] = 0
    s[5] = 1
    s[6] = 0
    s[7] = 0
    s[8] = 1
    s[9] = 1
    s[12] = 0
    s[13] = 1
    s[15] = 0
    s[16] = 1
    s[18] = 0
    s[19] = 0
    s[20] = 1

</t>
<t tx="T325">@first #!/usr/bin/python

import smtplib, sys, os, time

if len(sys.argv) != 5:
    print "Usage: %s freemailHost smtpPort fromId toId" % sys.argv[0]
    sys.exit(1)

serverHost = sys.argv[1]
smtpPort = sys.argv[2]
try:
    fromId = open(sys.argv[3]).read()
except:
    fromId = sys.argv[3]
try:
    toId = open(sys.argv[4]).read()
except:
    toId = sys.argv[4]

# create a message
msg = """Subject: Freemail test %s -&gt; %s

Hi %s,
    
This is a message. sent to you on %s

Hope you get it!

Cheers,
%s
""" % (fromId, toId, toId, time.asctime(), fromId)

fromHdr = "From: %s\r\n" % fromId
toHdr = "To: %s\r\n" % toId
date = "Date: %s\r\n" % time.asctime(time.localtime(time.time()))

msg = fromHdr + toHdr + date + msg

s = smtplib.SMTP(serverHost, smtpPort)
#s.sendmail(fredAddr, [maryAddr], msg)
s.sendmail(fromId, [toId], msg)
</t>
<t tx="T326">@first #!/usr/bin/python

import poplib, sys, os

if len(sys.argv) != 4:
    print "Usage: %s freemailHost popPort toId" % sys.argv[0]
    sys.exit(1)

serverHost = sys.argv[1]
popPort = sys.argv[2]
id = file(sys.argv[3]).read()

p = poplib.POP3(serverHost, popPort)
#p.user(maryAddr)
p.user(id)
p.pass_("blah")
p.list()

numMsgs, totalSize = p.stat()
if numMsgs == 0:
    print "*** NO MESSAGES ***"
else:
    for i in range(1, numMsgs + 1):
        (header, msg, octets) = p.retr(i)
        print "Message ", `i`, ':'
        for line in msg:
            print '   ' + line
        print '-----------------------'
    for i in range(1, numMsgs+1):
        p.dele(i)
p.quit()

</t>
<t tx="T327">@first #!/usr/bin/python

import code
try:
    import readline
except:
    pass

con = code.InteractiveConsole()

ps1 = "Your command? "
ps2 = "go on... "
ps = ps1

running = 1
cont = 0

while running:
    line = con.raw_input(ps)
    cmd = line.strip().split(" ", 1)[0]

    if not cont:
        if cmd == 'fuck':
            print "No bad language please"
        elif cmd == 'quit':
            running = 0
        else:
            res = con.push(line)
            #print "con.push returned %s" % res
            if res:
                cont = 1
                ps = ps2
    else:
        res = con.push(line)
        #print "con.push returned %s" % res
        if not res:
            cont = 0
            ps = ps1

</t>
<t tx="T328"># Makefile for SSLCrypto python extension

# if the make screws up, you might need to edit the 2 variables
# at the top of the file 'setup.py'.

SSLCrypto: SSLCrypto.so

SSLCrypto.so: src/SSLCrypto.c src/die.c
	python setup.py build_ext --inplace

clean:
	rm -rf SSLCrypto.so src/SSLCrypto.o src/die.o build

doco:
	epydoc -n "FreeMail API" -o doc/classes freemail.py
</t>
<t tx="T329">@first #!/usr/bin/env python

"""
release.py

Builds FreeMail into a releasable package, and copies it to the servers
"""

import re, sys, os

import freemail

import sys, os, commands
 
# util functions

def sh(cmd):
    print "Executing:", cmd
    out = commands.getoutput(cmd)
    print out

 
build = freemail.build

pyversion = '2.3'
name = "FreeMail"
tmpdir = "tmp"

webdir = "website"
releasedir = "/main/websites/freenet/python/freemail"

# hack the web pages and windows build scripts

def hackFile(filename):
    fd = open(filename, "rb")
    raw = fd.read()
    fd.close()
    raw1 = re.sub("FreeMail-alpha-[0-9][0-9][0-9]", "FreeMail-alpha-%s" % build, raw)
    fd = open(filename, "wb")
    fd.write(raw1)
    fd.close()
    #print raw1

hackFile("build.bat")
hackFile("wininstaller.nsi")
hackFile("website/body.html")

# put the changelog onto the site
sh("cp CHANGELOG website/CHANGELOG.txt")

# build fresh doco
 
#sh("make clean")
#sh("rm -rf doc/classes")
sh("make")
sh("make doco")
sh("rm -f src/*.o")


raw_input("Please run build.bat in windoze, press &lt;ENTER&gt; when it completes (or Ctrl-C if it fucks up): ")


files = ["freemail.py",
         "README", "INSTALL", "LICENSE", "CHANGELOG", "ROADMAP",
         "code.leo",
         "designNotes.txt",

         "start-freemail.sh",
         "stop-freemail.sh",
         "start-freemail.bat",

         # freenet files
         "freenet.py",
         # pyweb files
         "pyweb",
         # metakit files
         #"metakitplus.py",
         #"metakit.py",
         ]

filesWin = [#"Mk4py.dll",
            "SSLCrypto.pyd",
            "hackdb.py",
            ]

filesNix = [#"Mk4py.so",
            "hackdb.py",
            'console.py',
            "SSLCrypto.so",
            ]

filesGen = [#'metakit-2.4.9.2.tar.gz',
            #'console.py',
            "hackdb.py",
            "src",
            "Makefile", 'setup.py',
            ]

reldirNix = name+"-alpha-"+build+"-nix"
reldirWin = name+"-alpha-"+build+"-win"
reldirGen = name+"-alpha-"+build+"-generic"
reldirNomod = name+"-alpha-"+build+"-nomodules"

absdirNix = tmpdir+"/"+reldirNix
absdirWin = tmpdir+"/"+reldirWin
absdirGen = tmpdir+"/"+reldirGen
absdirNomod = tmpdir+"/"+reldirNomod
 
# build distro
 
oldcwd = os.getcwd()
sh("rm -rf "+absdirNix)
sh("rm -rf "+absdirWin)
sh("rm -rf "+absdirGen)
sh("rm -rf "+absdirNomod)
sh("mkdir "+absdirNix)
sh("mkdir "+absdirWin)
sh("mkdir "+absdirGen)
sh("mkdir "+absdirNomod)

tarfileNix = reldirNix+".tar.gz"
tarfileWin = reldirWin+".tar.gz"
tarfileGen = reldirGen+".tar.gz"
tarfileNomod = reldirNomod+".tar.gz"

# copy files into temp directories
sh("cp -rpL doc "+" ".join(files+filesNix)+" "+absdirNix)
sh("cp -rpL doc "+" ".join(files+filesWin)+" "+absdirWin)
sh("cp -rpL doc "+" ".join(files+filesGen)+" "+absdirGen)
sh("cp -rpL doc "+" ".join(files)+" "+absdirNomod)

#sh("chmod 755 %s/run-freemail.sh" % absdir)

# create the tarball
os.chdir(tmpdir)

sh("tar cfz "+tarfileNix+" "+reldirNix)
sh("tar cfz "+tarfileWin+" "+reldirWin)
sh("tar cfz "+tarfileGen+" "+reldirGen)
sh("tar cfz "+tarfileNomod+" "+reldirNomod)

# copy tarball and doco to website dir
sh("cp -r ../doc "+tarfileNix+" "+tarfileWin+" "+tarfileGen+" "+tarfileNomod+" ../website")

# clean up
sh("rm -rf tmp/*")

os.chdir(oldcwd)

# upload to wherever
resp = raw_input("Upload this build to website (Y/n) ? ")
if resp == '' or resp.lower()[0] == 'y':
    sh("rm -rf "+releasedir+"/*")
    sh("cp -rp website/* "+releasedir)
else:
    print "Not uploading to website"
</t>
<t tx="T330">from distutils.core import setup
from distutils.extension import Extension

try:
    from Pyrex.Distutils import build_ext
    gotPyrex = 1
except:
    gotPyrex = 0

import sys

sslLibs = ['crypto']
extra_link_args = []

if gotPyrex:
    setup(
      name = "SSLCrypto",
      version = '0.1',
      ext_modules=[ 
        Extension("SSLCrypto", ["src/SSLCrypto.pyx", 'src/die.c'],
                  libraries=sslLibs,
                  extra_link_args=extra_link_args)
        ],
      cmdclass = {'build_ext': build_ext}
    )
else:
    setup(
      name = "SSLCrypto",
      version = '0.1',
      ext_modules=[ 
        Extension("SSLCrypto", ["src/SSLCrypto.c", 'src/die.c'],
                  libraries=sslLibs,
                  extra_link_args=extra_link_args)
        ],
    )
</t>
<t tx="T331">"""
Setup script to build windows installer
"""

# setupwin.py
from distutils.core import setup
import py2exe

setup(name="freemail",
      scripts=["freemail.py"],
)

</t>
<t tx="T332">@first #!/usr/bin/python -i
@language python

import editobj
import cPickle
import sys

import freenet
import freemail
from freemail import cell, slotmap

import SSLCrypto

if len(sys.argv) == 1:
    dbfile = "freemail.dat"
else:
    dbfile = sys.argv[1]

def load():
    global m
    global db

    m = freemail.freemailServer(database=dbfile)
    db = m.db
    #db = cPickle.load(open(dbfile))

def edit():
    editobj.edit(db).mainloop()

def save():
    cPickle.dump(db, open(dbfile, "wb"), 1)

if __name__ == '__main__':
    load()
    print "database is in variable 'db'"
    print "type 'save()' to save changes, or 'load()' to reload the database"
    print "or, if you're brave, type 'edit()' to run a gui db editor"
    print
</t>
<t tx="T333"></t>
<t tx="T334"></t>
<t tx="T335">class POPserver:
    """
    Don't run more than one instance of this in a single process.
    """
    @others
</t>
<t tx="T336">def __init__(self, **kw):
    """
    Creates a POP server.
    
    No arguments.
    
    Keywords:
     - freemail - a freemailServer object - compulsory
     - bindaddr - TCP bind address, default '127.0.0.1'
     - port - default 110
     - handlerClass - a class constructor for a POP Request Handler
    """
    self.freemail = kw['freemail']
    self.bindaddr = kw.get('bindaddr', '')
    self.port = kw.get('port', 110)
    self.handlerClass = kw['handlerClass']

</t>
<t tx="T337">def exitNormal(self, signum, frame):
    sys.exit(0)
</t>
<t tx="T338">def run(self):

    #try:
    #    signal.signal(signal.SIGTERM, self.exitNormal)
    #    signal.signal(signal.SIGABRT, self.exitNormal)
    #except:
    #    print "Can't hook signals on this platform"

    self.server = SocketServer.ThreadingTCPServer(
        (self.bindaddr, self.port),
        self.handlerClass,
        )

    self.server.socket.setsockopt(
        socket.SOL_SOCKET,
        socket.SO_REUSEADDR,
        1)

    self.server.freemail = self.freemail

    print "** Freemail POP3 server now listening on port %d" % self.port

    while 1:
        self.server.handle_request()

</t>
<t tx="T339">class POPRequestHandler(SocketServer.StreamRequestHandler):
    @others
</t>
<t tx="T340">def __init__(self, sock, addr, server, **kw):
    
    #self.freemail = kw['freemail']
    #del kw['freemail']
    self.freemail = server.freemail
    self.log = self.freemail.log
    self.remotePeer = addr[0]

    #print "POPRequestHandler: args='%s' kw='%s'" % (str(args), str(kw))

    self.log(3, "POPRequestHandler: addr=%s" % str(addr))
    SocketServer.StreamRequestHandler.__init__(self, sock, addr, server, **kw)



</t>
<t tx="T341">COMMANDS = ("QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP",
            "RSET", "TOP", "UIDL", "USER", "PASS")
</t>
<t tx="T342">def handle(self):

    if self.remotePeer not in self.freemail.db.popHosts:
        self.server.close_request(self.request)
        return

    wf = self.request.makefile('wb')
    session = POPSession(wf, self.freemail)
    session.write_ok("POP3 server ready")
    wf.flush()

    #print "POPRequestHandler: self='%s'" % str(self)
    #print "POPRequestHandler: freemail='%s'" % str(self.freemail)

    while 1:
        try:
            line = self.request.recv(2048)
            if line == '':
                wf.close()
                self.server.close_request(self.request)
                return

            self.freemail.log(5, "POPRequestHandler: line='%s'" % line.strip())
            comm = line.split()
            if not comm:
                continue
            (cmd, args) = (comm[0].upper(), comm[1:])
            if cmd not in self.COMMANDS:
                raise Error, "Unknown command"
            method = getattr(session, cmd, None)
            if method is None:
                raise Error, "Unsupported command"
            method(*args)
        except (TypeError, Error), msg:
            wf.write("-ERR %s\r\n" % str(msg))
            if not isinstance(msg, Error):
                self.log(2, exceptionString())
            #self.log(2, "-ERR " + str(msg))
        wf.flush()
        if cmd == "QUIT":
            wf.close()
            self.server.close_request(self.request)
            return


</t>
<t tx="T343">
class POPSession:
    @others
</t>
<t tx="T344"># states
Q_AUTHORIZATION_USER = "AUTHORIZATION (USER)"
Q_AUTHORIZATION_PASS = "AUTHORIZATION (PASS)"
Q_TRANSACTION = "TRANSACTION"

</t>
<t tx="T345">def __init__(self, wf, freemail):
    self.write = wf.write
    self.state = POPSession.Q_AUTHORIZATION_USER
    self.username = None
    self.freemail = freemail
    self.log = freemail.log

</t>
<t tx="T346">def write_ok(self, msg):            # sends an OK response.
    self.log(5, "+OK " + str(msg))
    self.write("+OK %s\r\n" % msg)

</t>
<t tx="T347">def write_multi(self, lines):       # write "byte-stuffed" lines.
    for s in lines:
        if s[:1] == ".":            # termination octet?
            s = "." + s
        self.write(s + "\r\n")
</t>
<t tx="T348">def write_raw(self,str):
    #print str
    self.write(str)
</t>
<t tx="T349">def __check_state(self, STATE):
    if self.state is not STATE:
        raise Error, "Wrong state " + self.state
</t>
<t tx="T350">def CAPA(self):
    self.write_ok("Good bye...")
</t>
<t tx="T351">def QUIT(self):
    if self.state is POPSession.Q_TRANSACTION:
        self.maildrop.update()
    self.write_ok("Good bye...")
</t>
<t tx="T352">def USER(self, username):
    self.__check_state(POPSession.Q_AUTHORIZATION_USER)
    if not self.freemail.check_user(username):
        raise Error, "Unknown user %s" % username
    self.username = username
    self.state = POPSession.Q_AUTHORIZATION_PASS
    self.write_ok("User %s accepted" % username)
</t>
<t tx="T353">def PASS(self, password):
    self.__check_state(POPSession.Q_AUTHORIZATION_PASS)

    if self.freemail.auth(self.username, password) == "":
        self.state = POPSession.Q_AUTHORIZATION_USER
        POP3Logger("Failure to login as " + self.username, loglevel=1)
        raise Error, "Invalid password"

    self.username = self.freemail.check_user(self.username)

    self.maildrop = POPMaildrop(self.username, self.freemail)
    self.state = POPSession.Q_TRANSACTION
    POP3Logger("Successful login as " + self.username, loglevel=1)
    self.write_ok("Password accepted")

</t>
<t tx="T354">def STAT(self):

    self.__check_state(POPSession.Q_TRANSACTION)

    self.write_ok("%d %d" % (self.maildrop.msgcount,
                             self.maildrop.mailboxsize)
                  )
</t>
<t tx="T355">def LIST(self, msgno=None):

    self.__check_state(POPSession.Q_TRANSACTION)

    self.maildrop.update()
    mails = self.maildrop.mails

    self.log(5, "msgno = '%s'" % msgno)

    #print "MAILS:"
    #print mails
    
    if msgno is None:
        self.write_ok("scan listing begins")
        count = 0
        for (status, size, msg) in mails:
            count += 1
            line = "%d %d" % (count, size)
            self.write(line + "\r\n")
            #mydebug( line)
        self.write(".\r\n")
    else:
        msg = mails[_get_no(msgno)]
        self.write_ok("%s %d" % (msgno, msg.msgHash))

</t>
<t tx="T356">def RETR(self, msgno):

    self.__check_state(POPSession.Q_TRANSACTION)

    msg = self.maildrop.get_msg(_get_no(msgno))
    #print "STATUS:*%s*" % self.maildrop.get_msg_status(_get_no(msgno))
    #print "MSG:\r\n",msg
    #lines = msg.split("\r\n")
    self.write_ok("Sending message")
    self.write_raw(msg)
    self.write_raw("\r\n.\r\n")
</t>
<t tx="T357">def DELE(self, msgno):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.maildrop.delete_msg(_get_no(msgno))
    self.write_ok("Message deleted")
</t>
<t tx="T358">def NOOP(self):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.write_ok("Still here...")
</t>
<t tx="T359">def RSET(self):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.maildrop.reset()
    self.write_ok("Messages unmarked")</t>
<t tx="T360">def TOP(self, msgno, n):

    self.__check_state(POPSession.Q_TRANSACTION)
    try:
        n = int(n)
    except ValueError:
        raise Error, "Not a number"
    msg = self.maildrop.get_msg(_get_no(msgno))
    [head, body] = msg.split("\r\n\r\n", 1)
    self.write_ok("Top of message follows")
    self.write_multi(head.split("\r\n"))
    self.write("\r\n")
    self.write_multi(body.split("\r\n")[:n])
    self.write(".\r\n")
</t>
<t tx="T361">def UIDL(self, msgno=None):

    self.__check_state(POPSession.Q_TRANSACTION)
    if msgno is None:
        self.write_ok("UIDL listing begins") 
        count = 0           
        for (status, size, msg) in self.maildrop.mails:
            count += 1
            line = "%d %s" % (count, _digest(str(msg)))
            self.write(line + "\r\n")
            mydebug( line)
        self.write(".\r\n")
    else:
        msg = self.maildrop.get_msg(_get_no(msgno))
        self.write_ok("%s %s" % (msgno, _digest(msg)))
</t>
<t tx="T362">class POPMaildrop:
    @others
</t>
<t tx="T363">def __init__(self, username, freemail):
    self.username = username
    self.freemail = freemail
    self.maildrop = self
    self.db = freemail.db
    db = self.db
    self.log = freemail.log

    self._dbLock = freemail._dbLock
    self._dbUnlock = freemail._dbUnlock

    self.update()

    return
</t>
<t tx="T364">def get_stat(self):
    return self.msgcount, self.mailboxsize
</t>
<t tx="T365">def get_msg(self,msgno):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    freemail = self.freemail
    db = freemail.db
    config = db.config

    msgpath = os.path.join(config.storeDir, "rx", self.mails[msgno][2].msgHash)
    fd = open(msgpath, "rb")
    msg = fd.read()
    fd.close()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    return msg

    # old stuff from pop3 maildrop
    msgfile = open(self.maildir+"/"+self.mails[msgno][2], "r")
    msg = msgfile.read()
    msgfile.close()
    
    freemail.log(4, "*** Body of message follows...")
    freemail.log(4, msg)

    return msg

</t>
<t tx="T366">def delete_msg(self, msgno):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    db = self.db
    msg = self.mails[msgno][2]
    msg.isDeleted = 1

    self.log(4, "Marking message %d as deleted" % msgno)

    #del self.mails[msgno]

    self.freemail.dbSave()

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------
    return

</t>
<t tx="T367">def update(self):

    # -v-v-v-v-v-v- LOCK DATABASE ----------------
    self._dbLock()

    freemail = self.freemail
    db = freemail.db
    config = db.config

    # get a view of messages targetted at user
    vMsgs = db.rxMessages.select(idAddr=self.username, isDeleted=0)

    self.mails = []
    msgcount = 0
    totalsize = 0
    for rMsg in vMsgs:
        if not rMsg.isDeleted:
            itemsize = rMsg.msgLen
            totalsize += itemsize
            msgcount += 1
            self.mails.append( (0, itemsize, rMsg) )
    self.msgcount = msgcount 
    self.mailboxsize = totalsize

    freemail.log(5, "POPMaildrop: storeDir='%s'" % config.storeDir)
    self.maildir = os.path.join(config.storeDir, "rx")

    self._dbUnlock()
    # -^-^-^-^-^-^- UNLOCK DATABASE --------------

</t>
<t tx="T368">def reset(self):
    pass
</t>
<t tx="T369">def POP3Logger(message, loglevel=3):
    return
    print "pop3: %s" % message
</t>
<t tx="T370">def mydebug(msg):
    print msg
</t>
<t tx="T371">class Error (Exception):
    pass
</t>
<t tx="T372">def _digest(msg):
    i = msg.find("\r\nMessage-")
    if i &gt;= 0:
        if msg[i+10: i+14].upper() == "ID: ":
            j = msg.find("\r\n", i+14)
            if j &gt;= 0:
                msgid = msg[i+14: j]
                if len(msgid) &gt; 10:
                    return md5.new(msgid).hexdigest()
    return md5.new(msg).hexdigest()
</t>
<t tx="T373">def _get_no(msgno):
    try:
        return int(msgno) - 1
    except ValueError:
        raise Error, "Not a number"
</t>
<t tx="T374">@first #!/usr/bin/python -i



import sys, os
import metakitplus as mk

if len(sys.argv) &gt; 1:
    dbFile = sys.argv[1]
else:
    dbFile = "freemail.dat"

db = mk.storage(dbFile, 1)


</t>
<t tx="T375">@
Quick installation instructions for FreeMail.

--------------------------------------------
** If you have downloaded the generic version:

you'll need to unpack the metakit and SSLCrypto tarballs, follow the instructions
therein, and ensure you have the files Mk4py.so and SSLCrypto.so
either in your python site-packages directory, or the top-level
FreeMail directory

Note - correct build procedure for metakit is
$ tar xfz metakit-2.4.9.2
$ cd metakit-2.4.9.2/builds
$ ../unix/configure --with-python=/usr
$ make python
$ cp Mk4py.so ../..

(change the '/usr' to '/usr/local' if your python lives in /usr/local).

The correct build procedure for SSLCrypto is:
$ tar xfz SSLCrypto-0.1.1.tar.gz
$ cd SSLCrypto-0.1.1
$ python setup.py build
$ cp build/lib.linux-*/SSLCrypto.so ..

Hotes - to build these packages, you'll need the OpenSSL and Python development
packages (headers plus libs) installed on your system. Refer to the README and
INSTALL files within the metakit and SSLCrypto dirs.

------------------------------------------------------------------------
** If you have downloaded the source version with windows binary modules

you should see the files Mk4py.dll and SSLCrypto.pyd
in this directory, in which case FreeMail should run according
to the instructions in the README file


------------------------------------------------------------------
** If you have downloaded the source version with Linux/BSD binary modules

you should see the files Mk4py.so and SSLCrypto.so
in this top-level directory, in which case FreeMail **MAY** work
out of the box - refer to README.

If it crashes or locks up in strange places, (which will happen on some
linux distros like Gentoo), you will instead need to download the generic
version and build the binary modules.

------------------------------------------------------------------

Any problems with building - please contact david@freenet.org.nz
</t>
<t tx="T376">@language python
@others
</t>
<t tx="T377">__doc__ = """
This POP3 protocol deamon is a complete rewrite of popserve2.py 
See the documentation at the bottom for original notice 

This file implements a POP3 protocol server with integrated ISOS 
authentication. See http://www.kodart.net for details on ISOS.
Most recognizable code from Suzuki's version is the Session class. 
I have fixed several serious and minor bugs. You may consider the version 
you hold as a beta level, POP3(RFC1939) conformant, low performance deamon. 


It is distributed under GNU General Public License (see 
http://www.gnu.org/licenses/licenses.html), with no warranties. 
Use this program as is, just make sure you retain this and other notices of
contributors and don't charge anyone money for it!

For comments or suggestions you can contact me at mgencer@kodart.com

My version relies on an RPC server for authentication,
and provides mailbox modification. If you need another mechanism try creating
your own Authenticator class.

Enjoy!
Mehmet Gencer, mgencer@acikkanal.net


Below is the original popserve2 notice:

#####################################
##  H.Suzuki's original:           ##
#####################################
by H. Suzuki &lt;suzuki@acm.org&gt;
Simple POP3 Server 2 (1 September 2002)

This is a simple POP3 server written in Python.  It has backends to
read mails in RMAIL or mbox; thus it allows you to read Unix mails
from anywhere via POP3.

It runs on Python 2.2.*, Python 2.1.* and Jython 2.1.

The main class is PopServer; it constructs and uses Maildrop and Session
objects internally.  See RFC1939 for POP3.  This program is derived from
http://www.garshol.priv.no/download/software/python/popserve.py

I would ask that you refer to this program and the original popserve.py
when you make use of or incorporate this program into another product.
Bug reports and patches are welcome.
#####################################
##             END                 ##
#####################################

"""
</t>
<t tx="T378">import sys, os
import socket, SocketServer
import md5, traceback, signal
</t>
<t tx="T379">def POP3Logger(message, loglevel=3):
    print "pop3: %s" % message
</t>
<t tx="T380">def mydebug(msg):
    print msg
</t>
<t tx="T381">class Error (Exception):
    pass
</t>
<t tx="T382">class Authenticator:

    def __init__(self, userbase):
        self.userbase = userbase

    def check_user(self, attemptuser):
        if self.userbase.has_key(attemptuser):
            user = self.userbase[attemptuser]
        else:
            user = None
        if not user:
            return 0
        else:
            return 1

    def auth(self, attemptuser, passwd):
        if self.userbase.get(attemptuser, None) == passwd:
            return attemptuser
        else:
            return ""
</t>
<t tx="T383">class Maildrop:
    @others
</t>
<t tx="T384">def __init__(self,username):
    self.username = username
    self.maildir = os.path.join(gPrefixDir, username)
    self.mails = []
    mlist = os.listdir(self.maildir)
    msgcount = 0
    totalsize = 0
    for m in mlist:
        mfilestat = os.stat(self.maildir + "/" + m)
        itemsize = mfilestat[6]
        totalsize += itemsize
        msgcount += 1
        self.mails.append( (0, itemsize, m) )
    self.msgcount = msgcount 
    self.mailboxsize = totalsize

</t>
<t tx="T385">def get_stat(self):
    return self.msgcount, self.mailboxsize
</t>
<t tx="T386">def get_msg(self,msgno):
    msgfile = open(self.maildir+"/"+self.mails[msgno][2], "r")
    msg = msgfile.read()
    msgfile.close()
    return msg
</t>
<t tx="T387">def delete_msg(self,msgno):
    os.remove(self.maildir+"/"+self.mails[msgno][2])
</t>
<t tx="T388">def update(self):
    pass
</t>
<t tx="T389">def reset(self):
    pass
</t>
<t tx="T390">
class POPSession:
    @others
</t>
<t tx="T391"># states
Q_AUTHORIZATION_USER = "AUTHORIZATION (USER)"
Q_AUTHORIZATION_PASS = "AUTHORIZATION (PASS)"
Q_TRANSACTION = "TRANSACTION"

</t>
<t tx="T392">def __init__(self, wf):
    self.write = wf.write
    self.state = POPSession.Q_AUTHORIZATION_USER
    self.username = None
    self.maildrop = None
</t>
<t tx="T393">def write_ok(self, msg):            # sends an OK response.
    mydebug( "+OK " + str(msg))
    self.write("+OK %s\r\n" % msg)
</t>
<t tx="T394">def write_multi(self, lines):       # write "byte-stuffed" lines.
    for s in lines:
        if s[:1] == ".":            # termination octet?
            s = "." + s
        self.write(s + "\r\n")
</t>
<t tx="T395">def write_raw(self,str):
    #print str
    self.write(str)
</t>
<t tx="T396">def __check_state(self, STATE):
    if self.state is not STATE:
        raise Error, "Wrong state " + self.state
</t>
<t tx="T397">def QUIT(self):
    if self.state is POPSession.Q_TRANSACTION:
        self.maildrop.update()
    self.write_ok("Good bye...")
</t>
<t tx="T398">def USER(self, username):
    self.__check_state(POPSession.Q_AUTHORIZATION_USER)
    if not gAuthenticator.check_user(username):
        raise Error, "Unknown user %s" % username
    self.username = username
    self.state = POPSession.Q_AUTHORIZATION_PASS
    self.write_ok("User %s accepted" % username)
</t>
<t tx="T399">def PASS(self, password):
    self.__check_state(POPSession.Q_AUTHORIZATION_PASS)

    if gAuthenticator.auth(self.username, password) == "":
        self.state = POPSession.Q_AUTHORIZATION_USER
        POP3Logger("Failure to login as " + self.username, loglevel=1)
        raise Error, "Invalid password"

    self.maildrop = Maildrop(self.username)
    self.state = POPSession.Q_TRANSACTION
    POP3Logger("Successful login as " + self.username, loglevel=1)
    self.write_ok("Password accepted")
</t>
<t tx="T400">def STAT(self):

    self.__check_state(POPSession.Q_TRANSACTION)

    self.write_ok("%d %d" % (self.maildrop.msgcount,
                             self.maildrop.mailboxsize)
                  )
</t>
<t tx="T401">def LIST(self, msgno=None):

    self.__check_state(POPSession.Q_TRANSACTION)
    if msgno is None:
        self.write_ok("scan listing begins")
        count = 0
        for (status, size, msg) in self.maildrop.mails:
            count += 1
            line = "%d %d" % (count, size)
            self.write(line + "\r\n")
            mydebug( line)
        self.write(".\r\n")
    else:
        msg = self.maildrop.mails(_get_no(msgno))
        self.write_ok("%s %d" % (msgno, msg[1]))
</t>
<t tx="T402">def RETR(self, msgno):

    self.__check_state(POPSession.Q_TRANSACTION)

    msg = self.maildrop.get_msg(_get_no(msgno))
    #print "STATUS:*%s*" % self.maildrop.get_msg_status(_get_no(msgno))
    #print "MSG:\r\n",msg
    #lines = msg.split("\r\n")
    self.write_ok("Sending message")
    self.write_raw(msg)
    self.write_raw("\r\n.\r\n")
</t>
<t tx="T403">def DELE(self, msgno):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.maildrop.delete_msg(_get_no(msgno))
    self.write_ok("Message deleted")
</t>
<t tx="T404">def NOOP(self):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.write_ok("Still here...")
</t>
<t tx="T405">def RSET(self):

    self.__check_state(POPSession.Q_TRANSACTION)
    self.maildrop.reset()
    self.write_ok("Messages unmarked")
</t>
<t tx="T406">def TOP(self, msgno, n):

    self.__check_state(POPSession.Q_TRANSACTION)
    try:
        n = int(n)
    except ValueError:
        raise Error, "Not a number"
    msg = self.maildrop.get_msg(_get_no(msgno))
    [head, body] = msg.split("\r\n\r\n", 1)
    self.write_ok("Top of message follows")
    self.write_multi(head.split("\r\n"))
    self.write("\r\n")
    self.write_multi(body.split("\r\n")[:n])
    self.write(".\r\n")
</t>
<t tx="T407">def UIDL(self, msgno=None):

    self.__check_state(POPSession.Q_TRANSACTION)
    if msgno is None:
        self.write_ok("UIDL listing begins") 
        count = 0           
        for (status, size, msg) in self.maildrop.mails:
            count += 1
            line = "%d %s" % (count, _digest(str(msg)))
            self.write(line + "\r\n")
            mydebug( line)
        self.write(".\r\n")
    else:
        msg = self.maildrop.get_msg(_get_no(msgno))
        self.write_ok("%s %s" % (msgno, _digest(msg)))
</t>
<t tx="T408">def _digest(msg):
    i = msg.find("\r\nMessage-")
    if i &gt;= 0:
        if msg[i+10: i+14].upper() == "ID: ":
            j = msg.find("\r\n", i+14)
            if j &gt;= 0:
                msgid = msg[i+14: j]
                if len(msgid) &gt; 10:
                    return md5.new(msgid).hexdigest()
    return md5.new(msg).hexdigest()
</t>
<t tx="T409">def _get_no(msgno):
    try:
        return int(msgno) - 1
    except ValueError:
        raise Error, "Not a number"
</t>
<t tx="T410">
class POPRequestHandler(SocketServer.StreamRequestHandler):
    @others
</t>
<t tx="T411">COMMANDS = ("QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP",
            "RSET", "TOP", "UIDL", "USER", "PASS")
</t>
<t tx="T412">def handle(self):

    wf = self.request.makefile('wb')
    session = POPSession(wf)
    session.write_ok("POP3 server ready")
    wf.flush()

    while 1:
        try:
            line = self.request.recv(2048)
            comm = line.split()
            if not comm:
                continue
            (cmd, args) = (comm[0].upper(), comm[1:])
            if cmd not in self.COMMANDS:
                raise Error, "Unknown command"
            method = getattr(session, cmd, None)
            if method is None:
                raise Error, "Unsupported command"
            method(*args)
        except (TypeError, Error), msg:
            mydebug( "-ERR " + str(msg))
            wf.write("-ERR %s\r\n" % str(msg))
            if not isinstance(msg, Error):
                traceback.print_exc()
        wf.flush()
        if cmd == "QUIT":
            wf.close()
            self.server.close_request(self.request)
            return
</t>
<t tx="T413">class POPserver:
    """
    Don't run more than one instance of this in a single process.
    """
    def __init__(self, **kw):
        """
        Creates a POP server.
        
        No arguments.
        
        Keywords:
         - bindaddr - TCP bind address, default '127.0.0.1'
         - port - default 110
         - prefix - base directory of maildirs. Each user's maildir
           is a subdirectory of this one. Compulsory, no default.
         - userbase - a dict whose keys are POP3 usernames and values are
           plaintext passwords. Compulsory, no default
         - authenticator - a class instance which is compatible with the 'Authenticator'
           class in this module. Compulsory, no default
        """
        self.bindaddr = kw.get('bindaddr', '')
        self.port = kw.get('port', 110)
        self.prefix = kw['prefix']
        self.authenticator = kw['authenticator']

        global gAuthenticator
        gAuthenticator = self.authenticator

        global gPrefixDir
        gPrefixDir = self.prefix

    def exitNormal(self, signum, frame):
        sys.exit(0)

    def run(self):

        #try:
        #    signal.signal(signal.SIGTERM, self.exitNormal)
        #    signal.signal(signal.SIGABRT, self.exitNormal)
        #except:
        #    print "Can't hook signals on this platform"

        self.server = SocketServer.ThreadingTCPServer(
            (self.bindaddr, self.port),
            POPRequestHandler
            )

        self.server.socket.setsockopt(
            socket.SOL_SOCKET,
            socket.SO_REUSEADDR,
            1)

        while 1:
            self.server.handle_request()

</t>
<t tx="T414">def runPopServer():

    # Create an authenticator object
    userbase = {'testuser':'testpass'}
    authenticator = Authenticator(userbase)

    prefix = "popmail"

    # create dir prefix if nonexistent
    if not os.path.isdir(prefix):
        os.mkdir(prefix)
    for user in userbase.keys():
        fulldir = os.path.join(prefix, user)
        if not os.path.isdir(fulldir):
            os.mkdir(fulldir)

    # Create a server object
    serv = POPserver(bindaddr='',
                     prefix=prefix,
                     authenticator=authenticator,
                     )

    # and launch it
    serv.run()

if __name__ == '__main__':
    runPopServer()
</t>
<t tx="T415">@language python

"""A simple single-threaded POP3 server with a couple of maildrop
implementations. A more complete descendant can be found at
http://homepage.mac.com/suzuki_hisao/

$Id: popserve.py,v 1.2 2002/10/06 13:02:41 larsga Exp $
"""

@others
</t>
<t tx="T416">host = '127.0.0.1'
port = 110
</t>
<t tx="T417">import string, socket, rfc822, StringIO
</t>
<t tx="T418">def undeleted_msgs(msg):
    "Function used to filter out deleted messages from self.emails."
    return msg[0] != 1
</t>
<t tx="T419">class UserRegistry:
    """
    Used by the server to get information about users.
    """
    @others
</t>
<t tx="T420">def __init__(self):
    self.users = {}
</t>
<t tx="T421">def has_user(self,username):
    return self.users.has_key(username)
</t>
<t tx="T422">def correct_login(self,username,password):
    return self.users[username]==password
</t>
<t tx="T423">def get_maildrop(self,username):
    return SingleFileMaildrop(username)
</t>
<t tx="T424">class Maildrop:
    """
    Used by the server to gain access to a maildrop. All message numbers
    start with 0.
    """
    @others
</t>
<t tx="T425">def __init__(self,username):

    self.username=username
</t>
<t tx="T426">def get_msg_count(self):
    return 0
</t>
<t tx="T427">def get_msg_size_total(self):
    return 0
</t>
<t tx="T428">def delete_msg(self,msg_no):
    return 1
</t>
<t tx="T429">def msg_exists(self,msg_no):
    return "No such message"
    
</t>
<t tx="T430">def send_uidl(self,socket,msg_no=None):
    socket.send(".\r\n")
</t>
<t tx="T431">def send_list(self,socket,msg_no=None):
    socket.send(".\r\n")
    
</t>
<t tx="T432">def send_msg(self,msg_no,socket):
    return "Not implemented, sorry."
</t>
<t tx="T433">class SingleFileMaildrop(Maildrop):
    """
    A maildrop implemented by just reading in a single file and parsing it.
    """
    @others
</t>
<t tx="T434">def __init__(self,username):
    Maildrop.__init__(self,username)
    self.emails=[]
    self._read_mailfile(r"c:\minedo~1\temp\xfer\hjem\mailbox")
</t>
<t tx="T435">def _read_mailfile(self,mailfile):
    infile=open(mailfile)
    infile.readline()
    current=""
    
    while 1:
        line=infile.readline()
        if line=="":
            break
        elif line==chr(5)+"---"+chr(5)+"\n":
            self.emails.append((0,current))
            current=""
        else:
            current=current+line[:-1]+"\r\n"
            
    infile.close()
</t>
<t tx="T436">def get_msg_count(self):
    return len(self.emails)
</t>
<t tx="T437">def get_msg_size_total(self):
    sum=0
    for (deleted,email) in self.emails:
        if not deleted:
            sum=sum+len(email)
    return sum
</t>
<t tx="T438">def delete_msg(self,msg_no):
    try:
        (deleted, msg) = self.emails[msg_no]
        if deleted:
            return "Message already deleted"
        else:
            self.emails[msg_no] = (1, msg)
            return 1
    except IndexError,e:
        return "No such message"
</t>
<t tx="T439">def msg_exists(self,msg_no):
    try:
        (deleted, msg) = self.emails[msg_no]
        if deleted:
            return "Message has been deleted"
        else:
            return 1
    except IndexError,e:
        return "No such message"
            
</t>
<t tx="T440">def send_msg(self, msg_no, socket):
    try:
        (deleted, msg) = self.emails[msg_no]
        if deleted:
            return "Message has been deleted"
        else:
            socket.send(msg+"\r\n.\r\n")
            return 1
    except IndexError,e:
        return "No such message"
</t>
<t tx="T441">def send_uidl(self, socket, msg_no=None):

    if msg_no == None:
        mails = filter(undeleted_msgs,self.emails)
    else:
        mails = [self.emails[msg_no]]

    ix=1
    for (mark,mail) in mails:
        msg=rfc822.Message(StringIO(mail))
        id=msg["message-id"]
        uidl="%d %s" % (ix,id)
        socket.send(uidl+"\r\n")
        print uidl
        ix=ix+1

    socket.send(".\r\n")
</t>
<t tx="T442">def send_list(self, socket, msg_no=None):
    if msg_no == None:
        mails = filter(undeleted_msgs, self.emails)
    else:
        mails = [self.emails[msg_no]]

    ix = 1
    
    for (mark,mail) in mails:
        list = "%d %d" % (ix, len(mail))
        socket.send(list+"\r\n")
        print list
        ix = ix + 1

    socket.send(".\r\n")
</t>
<t tx="T443">class SingleUnixFileMaildrop(SingleFileMaildrop):
    """
    A maildrop that reads a UNIX RMAIL file.
    """
    @others
</t>
<t tx="T444">def _read_mailfile(self,mailfile):

    infile = open(r"c:\minedo~1\temp\xfer\hjem\rmail")
    inheader = 1
    current = ""
    
    while 1:           
        line = infile.readline()
        if line == "":
            break
        elif inheader and line == "*** EOOH ***\n":
            inheader = 0
        elif not inheader:
            if line == "\037\014\012":
                self.emails.append((0,current))
                current = ""
                inheader = 1
            else:
                current = current+line[:-1]+"\r\n"        
    infile.close()
</t>
<t tx="T445">class SingleFileMailMaildrop(SingleFileMaildrop):
    """
    A maildrop implemented by just reading in a single file and considering
    it an email.
    """
    @others
</t>
<t tx="T446">def _read_mailfile(self,mailfile):

    infile=open(r"c:\minedo~1\temp\xfer\mailbox")

    current=""        
    while 1:
        line=infile.readline()
        if line=="":
            break
        else:
            current=current+line[:-1]+"\r\n"

    self.emails.append((0,current))                
    infile.close()
    
</t>
<t tx="T447">class POPSession:
    """
    Represents a session from the user logs in until s/he logs out.

    Hacked, stripped down and otherwise bastardised from the original
    'popserve.py' by Lars M. Garshol:
     - http://www.garshol.priv.no/download/software/python/
     - http://homepage.mac.com/suzuki_hisao/
    Thanks Lars for saving me a slab of time! :)
    """
    @others
</t>
<t tx="T448">AUTHORIZATION_USER=0
AUTHORIZATION_PASS=1
TRANSACTION=2

# Negative numbers count a number of optional parameters
comm_params={"user":1,"pass":1,"stat":0,"quit":0,"dele":1,"noop":0,
             "retr":1,"uidl":-1,"list":-1}

</t>
<t tx="T449">def __init__(self,conn):

    self.userreg = UserRegistry()
    self.sessions = {}

    self.conn = conn
    self.state = Session.AUTHORIZATION_USER
    self.username = None
    self.maildrop = None

    self.send_ok(self.get_greeting())
    self.wait()
</t>
<t tx="T450">def wait(self):
    "Waits for client commands and dispatches them."

    while 1:
        comm=conn.recv(2048)
        print comm[:-1]

        comm=string.split(comm)
        comm[0]=string.lower(comm[0])

        # --- Checking the command

        OK=1
        try:
            if Session.comm_params[comm[0]]&lt;0:
                if len(comm)-1&gt;-1*Session.comm_params[comm[0]]:
                    self.error("Invalid number of arguments")
                    OK=0
            elif len(comm)-1!=Session.comm_params[comm[0]]:
                self.error("Invalid number of arguments")
                OK=0
        except KeyError:
            self.error("Unknown command")
            OK=0

        if not OK: continue

        # --- Dispatching commmand
        
        if comm[0]=="user":
            self.user(comm[1])
        elif comm[0]=="pass":
            self.password(comm[1])
        elif comm[0]=="stat":
            self.stat()
        elif comm[0]=="dele":
            self.dele(comm[1])
        elif comm[0]=="noop":
            self.noop()
        elif comm[0]=="retr":
            self.retr(comm[1])
        elif comm[0]=="quit":
            self.quit()
            break
        elif comm[0]=="uidl":
            if len(comm)==2:
                self.uidl(comm[1])
            else:
                self.uidl()
        elif comm[0]=="list":
            if len(comm)==2:
                self.list(comm[1])
            else:
                self.list()
        else:
            self.error("Unknown command %s" % comm[0])
</t>
<t tx="T451">def user(self,username):
    assert self.state==Session.AUTHORIZATION_USER

    if not self.userreg.has_user(username):
        self.error("Unknown user %s" % username)
    elif self.sessions.has_key(username):
        self.error("User %s already logged in" % username)
    else:
        self.sessions[username] = 1
        self.username=username
        self.state=Session.AUTHORIZATION_PASS
        self.send_ok("User %s accepted" % username)
</t>
<t tx="T452">def password(self,password):

    assert self.state==Session.AUTHORIZATION_PASS

    if self.userreg.correct_login(self.username,password):
        self.state = Session.TRANSACTION
        self.maildrop = self.get_maildrop(self.username)
        self.send_ok("Password accepted")
    else:
        self.error("Invalid password")
</t>
<t tx="T453">def stat(self):
    assert self.state==Session.TRANSACTION

    self.send_ok("%d %d" % (self.maildrop.get_msg_count(),
                            self.maildrop.get_msg_size_total()))
        
</t>
<t tx="T454">def dele(self,msg_no):
    assert self.state==Session.TRANSACTION

    try:
        msg_no=string.atoi(msg_no)-1
    except ValueError,e:
        self.error("Not a number")
        return
        
    resp=self.maildrop.delete_msg(msg_no)
    if resp==1:
        self.send_ok("Message deleted")
    else:
        self.error(resp)
</t>
<t tx="T455">def retr(self,msg_no):
    assert self.state==Session.TRANSACTION

    try:
        msg_no=string.atoi(msg_no)-1
    except ValueError,e:
        self.error("Not a number")
        return
    
    resp=self.maildrop.msg_exists(msg_no)
    if resp==1:
        self.send_ok("Sending message")
        self.maildrop.send_msg(msg_no,self.conn)
    else:
        self.error(resp)
        
</t>
<t tx="T456">def noop(self):
    assert self.state == Session.TRANSACTION
    
    self.send_ok("Still here...")
</t>
<t tx="T457">def uidl(self,msgno=None):
    assert self.state==Session.TRANSACTION
    
    if msgno!=None:
        try:
            msgno=string.atoi(msgno)-1
        except ValueError,e:
            self.error("Not a number")
            return

        resp=self.maildrop.msg_exists(msgno)            
        if resp!=1:
            self.error(resp)
            return

    self.send_ok("UIDL listing begins")
    self.maildrop.send_uidl(self.conn,msgno)
</t>
<t tx="T458">def list(self,msgno=None):
    assert self.state==Session.TRANSACTION
    
    if msgno!=None:
        try:
            msgno=string.atoi(msgno)-1
        except ValueError,e:
            self.error("Not a number")
            return

        resp=self.maildrop.msg_exists(msgno)            
        if resp!=1:
            self.error(resp)
            return

    self.send_ok("scan listing begins")
    self.maildrop.send_list(self.conn,msgno)
    
</t>
<t tx="T459">def quit(self):
    self.send_ok("Freemail POP3 Server: goodbye")
    if self.username != None:
        self._quit(self.username)
    self.conn.shutdown(2)        
    del self.sessions[username]
</t>
<t tx="T460">def get_greeting(self):
    return "Freemail POP3 Server ready"
    
</t>
<t tx="T461">def get_maildrop(self,username):
    return self.userreg.get_maildrop(username)
</t>
<t tx="T462">def error(self,msg):
    print "-ERR "+msg
    self.conn.send("-ERR %s\r\n" % msg)
</t>
<t tx="T463">def send_ok(self,msg):
    "Sends an OK response."
    print "+OK "+msg
    self.conn.send("+OK %s\r\n" % msg)
    
</t>
<t tx="T464"># --- Main program

def main():

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(host, port)    
    s.listen(1)   
    print "Ready (%s:%s)" % (host,port)
    
    while 1:
        conn, addr = s.accept()
        print 'Connected by:', addr
        POPSession(conn)

if __name__ == '__main__':
    main()
</t>
<t tx="T465"></t>
<t tx="T466">@first #!/usr/bin/env python

"""
pyproxy.py - Simple FProxy replacement
"""
@others
</t>
<t tx="T467">fcpHost = "hermes"
fcpPort = 8481
httpPort = 8887
allowedHosts = ['127.0.0.1', '192.168.1.2', '192.168.1.3', '192.168.2.1', '192.168.2.2']
logLevel = 2
version = "0.0"
defaultHtl = 15
</t>
<t tx="T468">import sys, os, time, base64, sha, re, string, random, fnmatch
import thread
import socket, SocketServer
import md5, traceback, signal
import BaseHTTPServer
import mimetypes

import posixpath
import BaseHTTPServer
import select
import urllib
import cgi
import shutil
import mimetypes
from StringIO import StringIO

from pdb import set_trace

# import some other modules that should be with this package
from pyweb import *
import freenet

freenet.verbosity(logLevel)

</t>
<t tx="T469">class WebUI:
    """
    Handles each incoming HTTP hist
    """
    @others
</t>
<t tx="T470">def __init__(self, req):
    """
    arg 'req' is a handle to an HTTPRequestHandler object
    """

    # set up the bare-bones page
    page = http(stream=req.wfile, streamin=req.rfile)
    self.page = page
    self.setCSS()

    page.title = "pyProxy Freenet Web Interface"

    session = page.session
    self.session = session

    fields = session.fields
    self.fields = fields

    owner = req.owner
    self.owner = req.owner

    self.req = req
    self.write = req.wfile.write

    self.log = self.session.log

    # do the interaction
    self.run()

    if page.session.fields.has_key('env'):
        self.envDump()

    if self.page:
        # run() can stick None into self.page if it is sending its
        # own content
        self.page.send()
</t>
<t tx="T471">def run(self):

    #set_trace()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req
    owner = self.owner
    host = req.headers.getheader("Host").split(":")[0]

    page.add(center(h3("PyProxy Freenet Web Interface"),
                    ))

    # temporary - allow HTTP environment dumps
    #print "fields='%s'" % repr(fields)
    #log(4, "fields='%s'" % repr(fields))
    #if fields.has_key("env"):
    #    self.envDump()

    path = session.env['SCRIPT_NAME']

    if path == "/" and not fields['geturi']:
        # stick up a simple front page
        TFE = "SSK@rBjVda8pC-Kq04jUurIAb8IzAGcPAgM/TFE//"
        FreenetHelp = "SSK@rjYFfgPHfolmcStiaoxESFfBXz8PAgM/FreenetHelp//"
        TFEE = "SSK@-w495UL3mfSlWC2c~nRAuG2fAWwPAgM/TFEE//"
        CofE = "SSK@9G4s~jLQJB7ALQg-v2q5xKAJy9YPAgM/CofE//"
        YoYo = "SSK@Sc6qV~D6iFhaYord6HtbjJ8MaEYPAgM/YoYo//"
        pyFreenet = "SSK@rdRr%7eqa898tOpfh4QPDV6mMY0jgPAgM/pyFreenet//"

        #page.add(center("Enter your URI in the address bar, as:", br(),
        #                "%s:%s/&amp;lt;my_uri&amp;gt;//" % (host, httpPort)))
        #return
                        
        page.add(table(attr(cellspacing=0,
                            cellpadding=0,
                            align='center',
                            border=0,
                            ),
                       tr(td(form(attr(method="POST", action="/"),
                                  b(big("Enter Freenet URI:")), br(),
                                  input(type='text', name='geturi', size=40), br(),
                                  b(big("Hops-to-Live")), br(),
                                  input(type='text', name='htl', size=3, value=defaultHtl), br(),
                                  input(type='submit', value='Retrieve'),
                                  ),
                             ),
                          ),
                       tr(td(b(big("Some Sample Freesites:")),
                             ul(li(a("The Freedom Engine", href=TFE)),
                                li(a("Freenet Help", href=FreenetHelp)),
                                li(a("The Tower", href=TFEE)),
                                li(a("Content of Evil", href=CofE)),
                                li(a("Yo-Yo", href=YoYo)),
                                li(a("pyFreenet", href=pyFreenet)),
                                ),
                             ),
                           ),
                       ),
                   )
        return

    # hitting something in-freenet
    if fields['geturi']:
        #log(4, "Y fields['geturi'] = '%s'" % repr(fields['geturi']))
        targeturi = fields['geturi']
        #log(4, "geturi='%s'" % targeturi)
        #fields['geturi'] = ''
    else:
        #log(4, "N fields['geturi'] = '%s'" % repr(fields['geturi']))
        targeturi = path[1:]

    targeturi = urllib.unquote(targeturi)

    # determine an absolute URL
    url = "http://%s:%s/%s" % (host, httpPort, targeturi)

    #if fields['getnow']:
    #    log(4, "got field 'getnow'")

    try:
        htl = int(fields['htl'])
    except:
        htl = defaultHtl

    # if the URI was passed via form, send a 'Requesting' page then get the page
    if fields['geturi']:
        # frig page header to put up a 'pending' page
        log(4, "Sending refresh page for '%s'" % url)
        page.head.add('&lt;meta HTTP-EQUIV="refresh" content="1; URL=%s"&gt;\n' % (url+"?getnow=1&amp;htl="+str(htl)))

        page.add(center("Now fetching Freenet URI:", br(),
                        big(b(targeturi)), br(),
                        "This could take a couple of minutes - please be patient",
                        ))
        return
    else:
        #log(4, "no uri field")
        pass

    #
    # do the request now
    #

    # create node object, verify we can talk FCP to it
    try:
        node = freenet.node(fcpHost, fcpPort, htl)
        #node._handshake()
    except:
        # notify of node connect failure
        page.add(center("Failed to connect to Freenet FCP port at:", br(),
                        big(b("%s:%s" % (fcpHost, fcpPort))),
                        a("Return to main page", href="/"),
                        )
                 )
        return

    # got live FCP connection - get the key
    #set_trace()
    try:
        k = node.get(targeturi)
    except freenet.FreenetDataNotFound:
        err = "DataNotFound"
        page.add(center("Failed to retrieve key:", br(),
                        big(b("'"+targeturi+"'")), br(),
                        br(),
                        "Data Not Found - try at higher HTL if you want", br(),
                        form(attr(method="POST", action="/"),
                             b(big("Enter Freenet URI:")), br(),
                             input(type='text', name='geturi', size=40, value=targeturi), br(),
                             b(big("Hops-to-Live")), br(),
                             input(type='text', name='htl', size=3, value=htl), br(),
                             input(type='submit', value='Retrieve'),
                             ),
                        a("Return to main page", href="/"),
                        ))
        return
    except freenet.FreenetRouteNotFound:
        page.add(center("Failed to retrieve key:", br(),
                        big(b("'"+targeturi+"'")), br(),
                        "Route Not Found", br(),
                        a("Return to main page", href="/"),
                        ))
        return
    except:
        s = StringIO()
        traceback.print_exc(stream=s)
        page.add(center("Failed to retrieve key:", br(),
                        big(b("'"+targeturi+"'")), br(),
                        "Exception:",
                        blockquote(pre(s.getvalue())),
                        a("Return to main page", href="/"),
                        ))
        return


    # now transmit the retrieved key
    #mimetype = "text/plain"
    #log(4, "metadata: %s" % str(k.metadata))
    #if k.metadata:
    #    #log(4, "metadata map: %s" % str(k.metadata.map))
    #    if k.metadata.map.has_key(''):
    #        if k.metadata.map[''].has_key('mimetype'):
    #            mimetype = k.metadata.map['']['mimetype']
    mimetype = k.mimetype

    #log(4, "mimetype = '%s'" % mimetype)
    self.page = None
    try:
        self.write("Content-Type: %s\n\n" % mimetype)
        self.write(str(k))
    except:
        log(2, "WebUI.run: client terminated connection")
        return

    # decide what page to show
    #showpage = fields.get('showpage', 'main')
    #getattr(self, 'page_'+showpage)()


</t>
<t tx="T472">def setCSS(self):
    """
    Adds the CSS stylesheet to the page
    """
    page = self.page

    style = self.page.style

    cellcolor = "#ffffc0"
    bgcolor = "#ffffc0"
    tablecolor = "#ffffc0"
    textcolor = "#008000"
    font =  'arial, helvetica, sans-serif'
    borderwidth = '0'

    linktextcolor = "#000080"
    linkbgcolor = bgcolor
    linkborderwidth = "thick"
    linkbordercolor = textcolor

    btntextcolor = bgcolor
    btnbgcolor = textcolor
    btnborderwidth = "thick"
    btnbordercolor = textcolor
    btntextsize = "larger"

    fldBgColor = bgcolor
    fldColor = textcolor
    fldFont = font

    style['body'] = 'background:'+bgcolor + ';color:'+textcolor + ';font-family:'+font
    style['table.block'] = 'background:'+cellcolor + ';border-width:'+borderwidth
    style['table.main'] = 'background:'+tablecolor + ';border-width:'+borderwidth
    style['table'] = 'background:'+cellcolor + ';border-width:'+borderwidth
    style['.block'] = 'background:'+cellcolor + ';border-width:'+borderwidth
    style['.mainblock'] = 'background:'+cellcolor +';border-width:'+borderwidth + ';height:100%'
    style['a'] = 'text-decoration:none' + ';color:'+linktextcolor + ';background-color:'+linkbgcolor
    style['a:hover'] = 'text-decoration:none' + ';color:'+linkbgcolor + ';background-color:'+linktextcolor

    style['.textbutton'] = 'text-decoration:none' \
                           + ';color:'+btntextcolor \
                           + ';background-color:'+btnbgcolor \
                           + ';padding: 2px 2px' \
                           + ':border-width: medium' \
                           + ':border-color:'+btnbordercolor # + ';font-size:'+btntextsize

    style['input'] = 'background:'+fldBgColor +';color:'+fldColor + ';font-family:'+fldFont
</t>
<t tx="T473"></t>
<t tx="T474">def page_main(self):

    #set_trace()

    page = self.page
    session = self.session
    fields = self.fields
    req = self.req

    freemail = self.freemail
    db = freemail.db
    config = db.config

    # get status values

    # node status
    n = freenet.node(config.fcpHost, config.fcpPort)
    try:
        n._handshake()
        systemStatus = 'Online'
    except:
        systemStatus = 'Offline'

    # number of identities
    numIdentities = len(db.identities)

    # peers
    numPeers = len(db.peers)

    # inbound messages
    numUnreadMessages = len(db.rxMessages.select(isDeleted=0))
    numUnsentMessages = len(db.txMessages.select(txStatus=TX_STATUS_NOT_SENT))
    numConfirmedMessages = len(db.txMessages.select(txStatus=TX_STATUS_CONFIRMED))
    numUnconfirmedMessages = len(db.txMessages.select(txStatus=TX_STATUS_AWAITING_RECEIPT))
    totalMessagesReceived = len(db.rxMessages)
    
    page.add(center(h3("Main Menu")))

    page.add(center(table(attr(cellspacing=0, cellpadding=3),
                          tr(td(b("System Status: ")),
                             td(systemStatus),
                             td(btnForm("Configure System", showpage="configureSystem")),
                             ),
                          tr(td(b("Number of Mailing Identities: ")),
                             td(numIdentities),
                             td(btnForm("Manage Identities", showpage="manageIdentities")),
                             ),
                          tr(td(b("Number of Peers: ")),
                             td(numPeers),
                             td(btnForm("Manage Peers", showpage="managePeers")),
                             ),
                          tr(td(b("Number of Unretrieved Messages: ")),
                             td(numUnreadMessages),
                             td(btnForm("Manage Inbound Messages", showpage="manageInbound")),
                             ),
                          tr(td(b("Number of Unsent Messages: ")),
                             td(numUnsentMessages),
                             td(btnForm("Manage Outbound Messages", showpage="manageOutbound")),
                             ),
                          tr(td(b("Number of Unconfirmed Messages: ")),
                             td(numUnconfirmedMessages),
                             td(btnForm("Manage Unconfirmed Messages", showpage="manageOutbound")),
                             ),
                          tr(td(b("Number of Confirmed Messages: ")),
                             td(numConfirmedMessages),
                             td(btnForm("Manage Confirmed Messages", showpage="manageOutbound")),
                             ),
                          tr(td(b("Total Messages Received: ")),
                             td(totalMessagesReceived),
                             td("&amp;nbsp;"),
                             ),
                          tr(td(attr(colspan=3, align='center'),
                                btnForm("Logout", "/logout")),
                             ),
                          )))
</t>
<t tx="T475">def page_(self):

    #set_trace()

    page = self.page
    session = self.session
    req = self.req
    fields = self.fields

    freemail = self.freemail
    db = freemail.db
    config = db.config

    page.add(center(h3("Page")))
    page.add(btnForm("Return to Main Page", showpage="main"))
</t>
<t tx="T476"></t>
<t tx="T477">def envDump(self):
    """
    Produces an HTTP environment dump for debugging purposes
    """
    page = self.page
    session = self.session
    fields = self.fields
    req = self.req
    owner = self.owner

    t = table(attr(cellspacing=0, cellpadding=3, border=1),
              tr(td(attr(colspan=2, align='left'),
                    b("HTTP Environment"))),
              tr(td(b("Name")), td(b("Value"))))
    
    for k,v in os.environ.items():
        t.add(tr(td(k), td(v)))
        
    tf = table(attr(cellspacing=0, cellpadding=3, border=1),
               tr(td(attr(colspan=2, align='center'),
                     b("HTTP fields"))),
               tr(td(b("Name")), td(b("Value"))))
    for k in session.fields.keys():
        v = session.fields[k]
        tf.add(tr(td(k), td(v)))
    
    page.add(t)
    page.add(tf)
</t>
<t tx="T478">class HTTPServer(SocketServer.ThreadingMixIn, BaseHTTPServer.HTTPServer):
#class HTTPServer(BaseHTTPServer.HTTPServer):

    @others
</t>
<t tx="T479">request_queue_size = 256
</t>
<t tx="T480">def __init__(self, **kw):
    """
    Creates a HTTP server.
    
    No arguments.
    
    Keywords:
     - owner - an object passed in by whoever owns this server - compulsory;
       note that this is available as the 'owner' attribute within
       request handler objects.
     - WebUI - a callable that handles incoming hits.
     - log - a function for logging which accepts 2 arguments:
         - a log level - 1=critical, 2=important, 3=trivial, 4=debug
     - port - default 8000
     - greeting - a text line to print to console when server fires up
     - handlerClass - a class constructor for a POP Request Handler
     - allowedHosts - list of allowed hosts
    """

    allowedHosts = kw.get('allowedHosts', None)
    if allowedHosts:
        bindAddr = ''
    else:
        bindAddr = '127.0.0.1'
        allowedHosts = ['127.0.0.1']
    port = kw.get('port', 8000)

    self.name = kw.get('name', "David's hacked-up HTTP server")
    self.log = kw['log']
    self.webUI = kw['webUI']
    self.owner = kw['owner']
    self.bindAddr = bindAddr
    self.port = port
    self.handlerClass = kw.get('handlerClass', HTTPRequestHandler)
    self.allowedHosts = allowedHosts

    self.request_queue_size = 256
    self.daemon_thread = False

    #set_trace()

    BaseHTTPServer.HTTPServer.__init__(self, (bindAddr, port), self.handlerClass)
</t>
<t tx="T481">def run(self):

    print "** "+self.name+" now listening on port %s" % self.port
    self.serve_forever()


</t>
<t tx="T482">class HTTPRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    @others

</t>
<t tx="T483"></t>
<t tx="T484">def __init__(self, request, client_address, server):

    self.server_version = "FreemailHTTP/" + version

    self.extensions_map = mimetypes.types_map.copy()
    self.extensions_map.update({
        '': 'application/octet-stream', # Default
        '.py': 'text/plain',
        '.c': 'text/plain',
        '.h': 'text/plain',
        })
    
    # Determine platform specifics
    self.have_fork = hasattr(os, 'fork')
    self.have_popen2 = hasattr(os, 'popen2')
    self.have_popen3 = hasattr(os, 'popen3')
    
    # Make rfile unbuffered -- we need to read one line and then pass
    # the rest to a subprocess, so we can't use buffered input.
    self.rbufsize = 0
    
    self.cgi_directories = ['/cgi-bin', '/htbin']
    
    self.nobody = None


   
    self.server = server
    self.client_address = client_address
    self.request = request
    self.owner = server.owner
    self.log = server.log
    
    BaseHTTPServer.BaseHTTPRequestHandler.__init__(self, request, client_address, server)</t>
<t tx="T485">def do_GET(self):
    """Serve a GET request."""
    self.run_cgi()
</t>
<t tx="T486">def do_POST(self):
    """Serve a POST request.

    This is only implemented for CGI scripts.

    """
    self.run_cgi()
</t>
<t tx="T487">def do_HEAD(self):
    """Serve a HEAD request."""
    self.run_cgi()
</t>
<t tx="T488">def run_cgi(self):
    """Execute a CGI script."""

    splitpath = os.path.split(self.path)
    #print "self.path='%s'" % self.path
    #print "splitted='%s'" % str(splitpath)
    dir, rest = splitpath

    i = rest.rfind('?')
    if i &gt;= 0:
        rest, query = rest[:i], rest[i+1:]
    else:
        query = ''

    #print "rest='%s' query='%s'" % (rest, query)

    i = rest.find('/')
    if i &gt;= 0:
        script, rest = rest[:i], rest[i:]
    else:
        script, rest = rest, ''
    #self.log(4, "self.path='%s'" % self.path)
    scriptname = dir + '/' + script
    #self.log(4, "scriptname='%s'" % scriptname)

    # I don't think we need this
    #scriptfile = self.translate_path(scriptname)
    #self.log(4, "scriptfile='%s'" % scriptfile)

    #self.log(4, "ATTEMPT: '%s" % self.translate_path(self.path))
    #self.log(4, "ATT: '%s" % urllib.unquote(self.path))

    if scriptname.startswith("//"):
        scriptname = scriptname[1:]
    #self.log(4, "scriptname='%s'" % scriptname)

    scriptname = urllib.unquote(self.path).split("?", 1)[0]

    # Reference: http://hoohoo.ncsa.uiuc.edu/cgi/env.html
    # XXX Much of the following could be prepared ahead of time!
    env = {}
    env['SERVER_SOFTWARE'] = self.version_string()
    env['SERVER_NAME'] = self.server.server_name
    env['GATEWAY_INTERFACE'] = 'CGI/1.1'
    env['SERVER_PROTOCOL'] = self.protocol_version
    env['SERVER_PORT'] = str(self.server.server_port)
    env['REQUEST_METHOD'] = self.command
    uqrest = urllib.unquote(rest)
    env['PATH_INFO'] = uqrest
    env['PATH_TRANSLATED'] = self.translate_path(uqrest)
    env['SCRIPT_NAME'] = scriptname
    if query:
        #print "query='%s'" % query
        env['QUERY_STRING'] = query
    host = self.address_string()
    if host != self.client_address[0]:
        env['REMOTE_HOST'] = host
    env['REMOTE_ADDR'] = self.client_address[0]
    # XXX AUTH_TYPE
    # XXX REMOTE_USER
    # XXX REMOTE_IDENT
    if self.headers.typeheader is None:
        env['CONTENT_TYPE'] = self.headers.type
    else:
        env['CONTENT_TYPE'] = self.headers.typeheader
    length = self.headers.getheader('content-length')
    if length:
        env['CONTENT_LENGTH'] = length
    accept = []
    for line in self.headers.getallmatchingheaders('accept'):
        if line[:1] in "\t\n\r ":
            accept.append(line.strip())
        else:
            accept = accept + line[7:].split(',')
    env['HTTP_ACCEPT'] = ','.join(accept)
    ua = self.headers.getheader('user-agent')
    if ua:
        env['HTTP_USER_AGENT'] = ua
    co = filter(None, self.headers.getheaders('cookie'))
    if co:
        env['HTTP_COOKIE'] = ', '.join(co)
    # XXX Other HTTP_* headers
    if not self.have_fork:
        # Since we're setting the env in the parent, provide empty
        # values to override previously set values
        for k in ('QUERY_STRING', 'REMOTE_HOST', 'CONTENT_LENGTH',
                  'HTTP_USER_AGENT', 'HTTP_COOKIE'):
            env.setdefault(k, "")

    # swap to our environment and launch script
    #oldenviron = os.environ
    #os.environ = env
    os.environ.update(env)

    # prevent unauthorised remote access
    if env['REMOTE_ADDR'] not in self.server.allowedHosts:
        self.log(1, "run_cgi: Rejecting REMOTE_ADDR '%s'" % env['REMOTE_ADDR'])
        self.send_error(403, "You cannot access this site from your host")
        return

    self.send_response(200, "Script output follows")
    try:
        # pass to the http ui generator
        self.server.webUI(self)
    except:
        self.server.handle_error(self.request, self.client_address)
    #os.environ = oldenviron

</t>
<t tx="T489">def translate_path(self, path):
    """Translate a /-separated PATH to the local filename syntax.

    Components that mean special things to the local file system
    (e.g. drive or directory names) are ignored.  (XXX They should
    probably be diagnosed.)

    """
    path = posixpath.normpath(urllib.unquote(path))
    words = path.split('/')
    words = filter(None, words)
    path = os.getcwd()
    for word in words:
        drive, word = os.path.splitdrive(word)
        head, word = os.path.split(word)
        if word in (os.curdir, os.pardir): continue
        path = os.path.join(path, word)
    return path

</t>
<t tx="T490">def log_message(self, format, *args):
    """
    
    """
    self.log(3, format%args)

</t>
<t tx="T491"></t>
<t tx="T492">def runServer(proxyobj=None):

    # Create a server object
    serv = HTTPServer(name="Freenet pyProxy Web Interface",
                      port=httpPort,
                      log=log,
                      owner=proxyobj,
                      handlerClass=HTTPRequestHandler,
                      allowedHosts=allowedHosts,
                      webUI=WebUI,
                     )

    # and launch it
    serv.run()
</t>
<t tx="T493">def log(level, msg):
    if level &lt;= logLevel:
        caller = traceback.extract_stack()[-2]
        full = "%s:%s:%s(): %s" % (caller[0], caller[1], caller[2], msg)
        #print "** %s" % msg
        print full
</t>
<t tx="T494">def main():
    runServer()

if __name__ == '__main__':
    main()
</t>
</tnodes>
</leo_file>
