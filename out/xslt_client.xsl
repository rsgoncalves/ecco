<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of ecco.
	
ecco is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0.
 
Copyright 2011-2013, The University of Manchester

ecco is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
General Public License as published by the Free Software Foundation, either version 3 of the 
License, or (at your option) any later version.

ecco is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with ecco.
If not, see http://www.gnu.org/licenses/
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ecco="http://owl.cs.manchester.ac.uk/diff">
	<xsl:output method="html" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>ecco</title>
				<meta charset="utf-8"/>
				<link rel="stylesheet" href="css/style.css"/>
				<link rel="stylesheet" href="css/reveal.css"/>
				<script type="text/javascript" src="js/jquery-1.4.4.js"/>
				<script type="text/javascript" src="js/jquery-ui-1.8.12.custom.min.js"/>
				<script type="text/javascript" src="js/script.js"/>
				<script type="text/javascript" src="js/jquery.checkboxtree.js"/>
				<script type="text/javascript" src="js/jquery.reveal.js"/>
				<script type="text/javascript" src="js/jscript.js"/>
			</head>
			<body>
				<h1><br/></h1>
				<xsl:variable name="uuid"><xsl:value-of select="/root/@uuid"/></xsl:variable>
				[<a href="index.jsp">Back Home</a>]
				<form action="getXml" method="post" class="inl">
					<xsl:if test="ends-with($uuid, 'lbl')">
						[<a id="top3" target="_blank" href="EccoChangeSet_labels.xml">Source XML</a>]
					</xsl:if>
					<xsl:if test="ends-with($uuid, 'gs')">
						[<a id="top3" target="_blank" href="EccoChangeSet_gensyms.xml">Source XML</a>]
					</xsl:if>
					<xsl:if test="not(ends-with($uuid, 'gs')) and not(ends-with($uuid, 'lbl'))">
						[<a id="top3" target="_blank" href="EccoChangeSet_names.xml">Source XML</a>]
					</xsl:if>
				</form><br/><br/>
				<h2 style="display:inline;">Change Summary</h2>&#160;
				[<a id="top1" href="javascript:;" onClick="showAll();$('#tree1').checkboxTree('expandAll');$('#tree2').checkboxTree('expandAll')">Show All</a> | 
				<a id="top2" href="javascript:;" onClick="hideAll();$('#tree1').checkboxTree('collapseAll');$('#tree2').checkboxTree('collapseAll');">Hide All</a>] 
				[<a id="genViewPerml" href="#" onclick="setPermalink();">Generate View Permalink</a>]
				<div id="view-plink-modal" class="reveal-modal">
					<h3>view-permalink</h3>
					<textarea id="view-plink" rows="3" cols="60" readonly="true" style="resize: none;"/><br/>
					[<a href="javascript:;" onclick="select_all('view-plink');">Select all</a>]
					<a class="close-reveal-modal">x</a>
				</div>
				<br/>
				<br/>
				<form action="output" method="post" name="GenSymTrigger">
					<input type="hidden" name="uuid"><xsl:attribute name="value"><xsl:value-of select="$uuid"/></xsl:attribute></input>
					<input type="button" name="gensyms" onClick="parent.location='index_labels.html'" value="Use Labels"/><xsl:text> </xsl:text>
					<input type="button" name="gensyms" onClick="parent.location='index_names.html'" value="Use Term Names"/><xsl:text> </xsl:text>
					<input type="button" name="gensyms" onClick="parent.location='index_gensyms.html'" value="Use GenSyms"/>
				</form>
				<table width="80%" style="table-layout:fixed;">
					<tr>
						<th class="topright">
							<input type="checkbox" name="removals" onClick="toggleGroup('removals')"/><xsl:text>  </xsl:text>
							<h3 style="display:inline;">Removals (<xsl:value-of select="/root/Removals/@size"/>)</h3>
						</th>
						<th class="topleft">
							<input type="checkbox" name="additions" onClick="toggleGroup('additions')"/><xsl:text>  </xsl:text>
							<h3 style="display:inline;">Additions (<xsl:value-of select="/root/Additions/@size"/>)</h3>
						</th>
					</tr>
					<tr>
						<td class="topright">
							<ul id="tree1">
								<li>
									<xsl:text>  </xsl:text>
									<input type="checkbox" name="effRemsTrigger" onClick="toggleChanges('effRems','effRemsTrigger')"/><xsl:text>  </xsl:text>
									<h4 style="display:inline;">Effectual (<xsl:value-of select="/root/Removals/Effectual/@size"/>)</h4><xsl:text>  </xsl:text>
									<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
										onmouseover="tooltip.show('Asserted axioms in Ontology 1, not present nor entailed by Ontology 2');"/>
								<ul>
									<xsl:variable name="wkst"><xsl:value-of select="/root/Removals/Effectual/Weakening/@size"/></xsl:variable>
									<xsl:variable name="wkrt"><xsl:value-of select="/root/Removals/Effectual/WeakeningWithRetiredTerms/@size"/></xsl:variable>
									<xsl:variable name="weakenings"><xsl:value-of select="$wkst + $wkrt"/></xsl:variable>
									<xsl:variable name="rmdst"><xsl:value-of select="/root/Removals/Effectual/RetiredModifiedDefinition/@size"/></xsl:variable>
									<xsl:variable name="rmdrt"><xsl:value-of select="/root/Removals/Effectual/RetiredModifiedDefinitionWithRetiredTerms/@size"/></xsl:variable>
									<xsl:variable name="rmodequiv"><xsl:value-of select="$rmdst + $rmdrt"></xsl:value-of></xsl:variable>
									<xsl:variable name="premst"><xsl:value-of select="/root/Removals/Effectual/PureRemoval/@size"/></xsl:variable>
									<xsl:variable name="premrt"><xsl:value-of select="/root/Removals/Effectual/PureRemovalWithRetiredTerms/@size"/></xsl:variable>
									<xsl:variable name="prem"><xsl:value-of select="$premst + $premrt"/></xsl:variable>
									<xsl:variable name="rdesc"><xsl:value-of select="/root/Removals/Effectual/RetiredDescription/@size"/></xsl:variable> 
									<xsl:if test="$weakenings = 0"><li><xsl:text>  </xsl:text>Weakenings (0)</li></xsl:if>
									<xsl:if test="$weakenings > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="effRems" onClick="toggleDiv('weak','weakrt');"/><xsl:text>  </xsl:text>
											<b>Weakenings (<xsl:value-of select="$weakenings"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Removed axioms that are more constraining than existing ones in Ontology 2');"/>
											<ul style="display:none">
												<xsl:if test="$wkst = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Shared Terms (0)</li></xsl:if>
												<xsl:if test="$wkst > 0">
													<li id="weaktrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('weak');"/>
														<xsl:text>  </xsl:text><a href="#weak">With Shared Terms (<xsl:value-of select="$wkst"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$wkrt = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Retired Terms (0)</li></xsl:if>
												<xsl:if test="$wkrt > 0">
													<li id="weakrttrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('weakrt');"/>
														<xsl:text>  </xsl:text><a href="#weakrt">With Retired Terms (<xsl:value-of select="$wkrt"/>)</a>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<xsl:if test="$rmodequiv = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Modified Definitions (0)</li></xsl:if>
									<xsl:if test="$rmodequiv > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="effRems" onClick="toggleDiv('wkequiv','wkequivrt');"/><xsl:text>  </xsl:text>
											<b>Modified Definitions (<xsl:value-of select="$rmodequiv"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Equivalence axioms that have been extended; if split into subsumptions then one was strengthened and the other weakened');"/>
											<ul style="display:none">
												<xsl:if test="$rmdst = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Shared Terms (0)</li></xsl:if>
												<xsl:if test="$rmdst > 0">
													<li id="wkequivtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('wkequiv');"/>
														<xsl:text>  </xsl:text><a href="#wkequiv">With Shared Terms (<xsl:value-of select="$rmdst"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$rmdrt = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Retired Terms (0)</li></xsl:if>
												<xsl:if test="$rmdrt > 0">
													<li id="wkequivrttrig" >
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('wkequivrt');"/>
														<xsl:text>  </xsl:text><a href="#wkequivrt">With Retired Terms (<xsl:value-of select="$rmdrt"/>)</a>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<xsl:if test="$rdesc = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Retired Descriptions (0)</li></xsl:if>
									<xsl:if test="$rdesc > 0">
										<li id="retdesctrig">
											<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
											<input type="checkbox" name="effRems" onClick="toggleDiv('retdesc');"/><xsl:text>  </xsl:text>
											<a href="#retdesc">Retired Descriptions (<xsl:value-of select="$rdesc"/>)</a><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();" 
												onmouseover="tooltip.show('Axioms describing retired terms, i.e., terms that no longer exist in Ontology 2');"/> 
										</li>
									</xsl:if>
									<xsl:if test="$prem = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Pure Removals (0)</li></xsl:if>
									<xsl:if test="$prem > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="effRems" onClick="toggleDiv('rem','remrt');"/><xsl:text>  </xsl:text>
											<b>Pure Removals (<xsl:value-of select="$prem"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Removed axioms with no identifiable relation with axioms in Ontology 2, typically related to hierachy changes');"/>
											<ul style="display:none">
												<xsl:if test="$premst = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Shared Terms (0)</li></xsl:if>
												<xsl:if test="$premst > 0">
													<li id="remtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('rem');"/>
														<xsl:text>  </xsl:text><a href="#rem">With Shared Terms (<xsl:value-of select="$premst"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$premrt = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Retired Terms (0)</li></xsl:if>
												<xsl:if test="$premrt > 0">
													<li id="remrttrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('remrt');"/>
														<xsl:text>  </xsl:text><a href="#remrt">With Retired Terms (<xsl:value-of select="$premrt"/>)</a>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<br/>
								</ul>
								</li><br/>
								<li>
									<xsl:text>  </xsl:text>
									<input type="checkbox" name="ineffRemsTrigger" onClick="toggleChanges('ineffRems','ineffRemsTrigger')"/><xsl:text>  </xsl:text>
									<h4 style="display:inline;">Ineffectual (<xsl:value-of select="/root/Removals/Ineffectual/@size"/>)</h4><xsl:text>  </xsl:text>
									<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
										onmouseover="tooltip.show('Asserted axioms in Ontology 1, that are not asserted but entailed by Ontology 2; they were removed but are still entailed');"/>
								<ul>
									<xsl:variable name="rreshuf"><xsl:value-of select="/root/Removals/Ineffectual/RemovedProspectiveRedundancy/RemovedReshuffleProspectiveRedundancy/@size"/></xsl:variable>
									<xsl:variable name="rprospnewred">
										<xsl:value-of select="/root/Removals/Ineffectual/RemovedProspectiveRedundancy/RemovedNewProspectiveRedundancy/@size"/>
									</xsl:variable>
									<xsl:variable name="rprospred"><xsl:value-of select="$rreshuf + $rprospnewred"/></xsl:variable>
									<xsl:variable name="rcrewrite"><xsl:value-of select="/root/Removals/Ineffectual/RemovedRewrite/RemovedCompleteRewrite/@size"/></xsl:variable>
									<xsl:variable name="rprewrite"><xsl:value-of select="/root/Removals/Ineffectual/RemovedRewrite/RemovedPartialRewrite/@size"/></xsl:variable>
									<xsl:variable name="rrewritten"><xsl:value-of select="$rcrewrite + $rprewrite"/></xsl:variable>
									<xsl:variable name="rredundant"><xsl:value-of select="/root/Removals/Ineffectual/RemovedRedundancy/@size"/></xsl:variable>
									<xsl:if test="$rprospred = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Prospective Redundancy (0)</li></xsl:if>
									<xsl:if test="$rprospred > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="ineffRems" onClick="toggleDiv('ravred','rst','rpsnovred');"/>
											<xsl:text>  </xsl:text>
											<b>Prospective Redundancy (<xsl:value-of select="$rprospred"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Removed axioms that would be redundant in Ontology 2 if not removed. These potentially have more constraining related axioms in Ontology 2');"/> 
											<ul style="display:none">
												<xsl:if test="$rreshuf = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Reshuffle (0)</li></xsl:if>
												<xsl:if test="$rreshuf > 0">
													<li id="ravredtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffRems" onClick="toggleDiv('ravred');"/>
														<xsl:text>  </xsl:text><a href="#ravred">Reshuffle (<xsl:value-of select="$rreshuf"/>)</a>
														<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
															onmouseover="tooltip.show('Removed axioms which are entailed by Ontology 2 due to apparent reshuffling of axioms or terms');"/>
													</li>
												</xsl:if>
												<xsl:if test="$rprospnewred = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;New (0)</li></xsl:if>
												<xsl:if test="$rprospnewred > 0">
													<li><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffRems" onClick="toggleDiv('rst','rpsnovred');"/>
														<xsl:text>  </xsl:text>
														<a href="#rst">New (<xsl:value-of select="$rprospnewred"/>)</a><b id="t1"/><xsl:text>  </xsl:text>
														<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
															onmouseover="tooltip.show('Removed axioms for which there are more constraining related axioms in Ontology 2');"/>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<xsl:if test="$rrewritten = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Rewritten (0)</li></xsl:if>
									<xsl:if test="$rrewritten > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="ineffRems" onClick="toggleDiv('rrewrite','rprw');"/>
											<xsl:text>  </xsl:text>
											<b>Rewritten (<xsl:value-of select="$rrewritten"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Axioms in Ontology 1 that are rewrites of axioms in Ontology 2');"/>
											<ul style="display:none">
												<xsl:if test="$rcrewrite = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Complete (0)</li></xsl:if>
												<xsl:if test="$rcrewrite > 0">
													<li id="rrewritetrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffRems" onClick="toggleDiv('rrewrite');"/>
														<xsl:text>  </xsl:text><a href="#rrewrite">Complete (<xsl:value-of select="$rcrewrite"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$rprewrite = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Partial (0)</li></xsl:if>
												<xsl:if test="$rprewrite > 0">
													<li id="rprwtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffRems" onClick="toggleDiv('rprw');"/>
														<xsl:text>  </xsl:text><a href="#rprw">Partial (<xsl:value-of select="$rprewrite"/>)</a>
													</li>
												</xsl:if>
											</ul> 
										</li>
									</xsl:if>
									<xsl:if test="$rredundant = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Redundant (0)</li></xsl:if>
									<xsl:if test="$rredundant > 0">
										<li id="rredtrig">
											<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
											<input type="checkbox" name="ineffRems" onClick="toggleDiv('rred')"/><xsl:text>  </xsl:text>
											<a href="#rred">Redundant (<xsl:value-of select="$rredundant"/>)</a><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Axioms in Ontology 1 that are entailed by at least one shared axiom between Ontology 1 and 2');"/> 
										</li>
									</xsl:if>
								</ul>
								</li>
							</ul>
						</td>
						<td class="topleft" style="vertical-align:top">
							<ul id="tree2">
								<li>
									<xsl:text>  </xsl:text>
									<input type="checkbox" name="effAddsTrigger" onClick="toggleChanges('effAdds','effAddsTrigger')"/><xsl:text>  </xsl:text>
									<h4 style="display:inline;">Effectual (<xsl:value-of select="/root/Additions/Effectual/@size"/>)</h4><xsl:text>  </xsl:text>
									<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
										onmouseover="tooltip.show('Asserted axioms in Ontology 2, not present nor entailed by Ontology 1');"/> 
								<xsl:variable name="stst"><xsl:value-of select="/root/Additions/Effectual/Strengthening/@size"/></xsl:variable>
								<xsl:variable name="stnt"><xsl:value-of select="/root/Additions/Effectual/StrengtheningWithNewTerms/@size"/></xsl:variable>
								<xsl:variable name="strengthenings"><xsl:value-of select="$stst + $stnt"/></xsl:variable>
								<xsl:variable name="mdst"><xsl:value-of select="/root/Additions/Effectual/NewModifiedDefinition/@size"/></xsl:variable>
								<xsl:variable name="mdnt"><xsl:value-of select="/root/Additions/Effectual/NewModifiedDefinitionWithNewTerms/@size"/></xsl:variable>
								<xsl:variable name="amodequiv"><xsl:value-of select="$mdst + $mdnt"/></xsl:variable>
								<xsl:variable name="newdescription"> <xsl:value-of select="/root/Additions/Effectual/NewDescription/@size"/></xsl:variable>
								<xsl:variable name="paddst"><xsl:value-of select="/root/Additions/Effectual/PureAddition/@size"/></xsl:variable>
								<xsl:variable name="paddnt"><xsl:value-of select="/root/Additions/Effectual/PureAdditionWithNewTerms/@size"/></xsl:variable>
								<xsl:variable name="padd"><xsl:value-of select="$paddst + $paddnt"/></xsl:variable>
								<ul>
									<xsl:if test="$strengthenings = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Strengthenings (0)</li></xsl:if>
									<xsl:if test="$strengthenings > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="effAdds" onClick="toggleDiv('st','stnt');"/><xsl:text>  </xsl:text>
											<b>Strengthenings (<xsl:value-of select="$strengthenings"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Axioms that are more constraining than existing axioms in Ontology 1');"/>
											<ul style="display:none">
												<xsl:if test="$stst = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Shared Terms (0)</li></xsl:if>
												<xsl:if test="$stst > 0">
													<li id="sttrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('st');"/>
														<xsl:text>  </xsl:text><a href="#st">With Shared Terms (<xsl:value-of select="$stst"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$stnt = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With New Terms (0)</li></xsl:if>
												<xsl:if test="$stnt > 0">
													<li id="stnttrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('stnt');"/>
														<xsl:text>  </xsl:text><a href="#stnt">With New Terms (<xsl:value-of select="$stnt"/>)</a>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<xsl:if test="$amodequiv = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Modified Definitions (0)</li></xsl:if>
									<xsl:if test="$amodequiv > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="effAdds" onClick="toggleDiv('stequiv','stequivnt');"/><xsl:text>  </xsl:text>
											<b>Modified Definitions (<xsl:value-of select="$amodequiv"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Equivalence axioms that have been extended; if split into subsumptions then one was strengthened and the other weakened');"/>
											<ul style="display:none">
												<xsl:if test="$mdst = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Shared Terms (0)</li></xsl:if>
												<xsl:if test="$mdst > 0">
													<li id="stequivtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('stequiv');"/>
														<xsl:text>  </xsl:text><a href="#aed">With Shared Terms (<xsl:value-of select="$mdst"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$mdnt = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With New Terms (0)</li></xsl:if>
												<xsl:if test="$mdnt > 0">
													<li id="stequivnttrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('stequivnt');"/>
														<xsl:text>  </xsl:text><a href="#aednt">With New Terms (<xsl:value-of select="$mdnt"/>)</a>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<xsl:if test="$newdescription = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;New Descriptions (0)</li></xsl:if>
									<xsl:if test="$newdescription > 0">
										<li id="newdesctrig">
											<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
											<input type="checkbox" name="effAdds" onClick="toggleDiv('newdesc');"/><xsl:text>  </xsl:text>
											<a href="#newdesc">New Descriptions (<xsl:value-of select="$newdescription"/>)</a><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Axioms describing newly created terms, i.e., terms that did not exist in Ontology 1');"/>
										</li>
									</xsl:if>
									<xsl:if test="$padd = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Pure Additions (0)</li></xsl:if>
									<xsl:if test="$padd > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="effAdds" onClick="toggleDiv('add','addnt');"/><xsl:text>  </xsl:text>
											<b>Pure Additions (<xsl:value-of select="$padd"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Added axioms with no identifiable relation with axioms in Ontology 1, typically related to hierachy changes');"/> 
											<ul style="display:none">
												<xsl:if test="$paddst = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With Shared Terms (0)</li></xsl:if>
												<xsl:if test="$paddst > 0">
													<li id="addtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('add');"/>
														<xsl:text>  </xsl:text><a href="#add">With Shared Terms (<xsl:value-of select="$paddst"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$paddnt = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;With New Terms (0)</li></xsl:if>
												<xsl:if test="$paddnt > 0">
													<li id="addnttrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="effAdds" onClick="toggleDiv('addnt');"/>
														<xsl:text>  </xsl:text><a href="#addnt">With New Terms (<xsl:value-of select="$paddnt"/>)</a>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
								</ul></li><br/>
								<li>
									<xsl:text>  </xsl:text>
									<input type="checkbox" name="ineffAddsTrigger" onClick="toggleChanges('ineffAdds','ineffAddsTrigger')"/><xsl:text>  </xsl:text>
									<h4 style="display:inline;">Ineffectual (<xsl:value-of select="/root/Additions/Ineffectual/@size"/>)</h4><xsl:text>  </xsl:text>
									<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
										onmouseover="tooltip.show('Asserted axioms in Ontology 2, that are not asserted but entailed by Ontology 1; they were added but were already entailed');"
									/> 
								<ul>
									<xsl:variable name="aprospresred">
										<xsl:value-of select="/root/Additions/Ineffectual/AddedProspectiveRedundancy/AddedReshuffleProspectiveRedundancy/@size"/>
									</xsl:variable>
									<xsl:variable name="aprospnewred">
										<xsl:value-of select="/root/Additions/Ineffectual/AddedProspectiveRedundancy/AddedNewProspectiveRedundancy/@size"/>
									</xsl:variable>
									<xsl:variable name="addedprospred"><xsl:value-of select="$aprospnewred + $aprospresred"/></xsl:variable>
									<xsl:variable name="addedrewrite"><xsl:value-of select="/root/Additions/Ineffectual/AddedRewrite/AddedCompleteRewrite/@size"/></xsl:variable>
									<xsl:variable name="addedprewrite"><xsl:value-of select="/root/Additions/Ineffectual/AddedRewrite/AddedPartialRewrite/@size"/></xsl:variable>
									<xsl:variable name="addedrewrites"><xsl:value-of select="$addedrewrite + $addedprewrite"/></xsl:variable>
									<xsl:variable name="addedredundant"><xsl:value-of select="/root/Additions/Ineffectual/AddedRedundancy/@size"/></xsl:variable>
									<xsl:if test="$addedprospred = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Retrospective Redundancy (0)</li></xsl:if>
									<xsl:if test="$addedprospred > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="ineffAdds" onClick="toggleDiv('aavred','apseudopred','aweak');"/>
											<xsl:text>  </xsl:text>
											<b>Retrospective Redundancy (<xsl:value-of select="$addedprospred"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Added axioms that would have been redundant in Ontology 1. These potentially have more constraining related axioms in Ontology 1');"/> 
											<ul style="display:none">
												<xsl:if test="$aprospresred = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Reshuffle (0)</li></xsl:if>
												<xsl:if test="$aprospresred > 0">
													<li id="aavredtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffAdds" onClick="toggleDiv('aavred');"/>
														<xsl:text>  </xsl:text><a href="#aavred">Reshuffle (<xsl:value-of select="$aprospresred"/>)</a>
														<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
															onmouseover="tooltip.show('Added axioms which were entailed by Ontology 1 due to apparent reshuffling of axioms or terms');"/>
													</li>
												</xsl:if>
												<xsl:if test="$aprospnewred = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;New (0)</li></xsl:if>
												<xsl:if test="$aprospnewred > 0">
													<li><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffAdds" onClick="toggleDiv('aweak','apseudopred');"/>
														<xsl:text>  </xsl:text>
														<a href="#aweak">New (<xsl:value-of select="$aprospnewred"/>)</a><xsl:text>  </xsl:text>
														<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
															onmouseover="tooltip.show('Added axioms for which there are more constraining related axioms in Ontology 1');"/>
													</li>
												</xsl:if>
											</ul>
										</li>
									</xsl:if>
									<xsl:if test="$addedrewrites = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Rewritten (0)</li></xsl:if>
									<xsl:if test="$addedrewrites > 0">
										<li><xsl:text>  </xsl:text>
											<input type="checkbox" name="ineffAdds" onClick="toggleDiv('arewrite','aprw');"/>
											<xsl:text>  </xsl:text>
											<b>Rewritten (<xsl:value-of select="$addedrewrites"/>)</b><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Axioms in Ontology 2 that are rewrites of axioms in Ontology 1');"/>
											<ul style="display:none">
												<xsl:if test="$addedrewrite = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Complete (0)</li></xsl:if>
												<xsl:if test="$addedrewrite > 0">
													<li id="arewritetrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffAdds" onClick="toggleDiv('arewrite');"/>
														<xsl:text>  </xsl:text><a href="#arewrite">Complete (<xsl:value-of select="$addedrewrite"/>)</a>
													</li>
												</xsl:if>
												<xsl:if test="$addedprewrite = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Partial (0)</li></xsl:if>
												<xsl:if test="$addedprewrite > 0">
													<li id="aprwtrig">
														<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
														<input type="checkbox" name="ineffAdds" onClick="toggleDiv('aprw');"/>
														<xsl:text>  </xsl:text><a href="#aprw">Partial (<xsl:value-of select="$addedprewrite"/>)</a>
													</li>
												</xsl:if>
											</ul> 
										</li>
									</xsl:if>
									<xsl:if test="$addedredundant = 0"><li><img src="images/blank.png" alt=""></img>&#160;&#160;Redundant (0)</li></xsl:if>
									<xsl:if test="$addedredundant > 0">
										<li id="aredtrig">
											<img src="images/blank.png" alt=""></img><xsl:text>  </xsl:text>
											<input type="checkbox" name="ineffAdds" onClick="toggleDiv('ared')"/><xsl:text>  </xsl:text>
											<a href="#ared">Redundant (<xsl:value-of select="$addedredundant"/>)</a><xsl:text>  </xsl:text>
											<img src="images/info_bubble.png" alt="" align="right" width="14" height="14" class="hotspot" onmouseout="tooltip.hide();"
												onmouseover="tooltip.show('Axioms in Ontology 2 that are entailed by at least one shared axiom between Ontology 1 and 2');"/> 
										</li>
									</xsl:if>
								</ul>
								</li>
							</ul>
						</td>
					</tr>
					<tr>
						<td class="topright">[<a href="javascript:void(0);" id="tree1-expandAll">Expand All</a> | <a href="javascript:void(0);" id="tree1-collapseAll">Collapse All</a>]</td>
						<td class="topleft">[<a href="javascript:void(0);" id="tree2-expandAll">Expand All</a> | <a href="javascript:void(0);" id="tree2-collapseAll">Collapse All</a>]</td>
					</tr>
				</table>
				<table width="100%" style="table-layout:fixed;">
					<colgroup><col/><col width="47.5%"/><col width="47.5%"/></colgroup>
					<xsl:apply-templates/>
					<tr class="withoutstyle"><td>&#160;</td></tr>
				</table>
				<div id="shared" style="display:none" class="footnote">Shared axiom between Ontology 1 and 2.</div>
			</body>
		</html>
	</xsl:template>


	<!--                     EFFECTUAL ADDITIONS                     -->


	<!-- Strengthenings w/ Shared Terms-->
	<xsl:template match="Strengthening">
		<xsl:if test="child::node()">
			<tbody id="st" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Strengthenings with Shared Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Weaker Axiom in Ontology 1</th>
					<th class="addition">Strengthened Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Strengthenings w/ New Terms -->
	<xsl:template match="StrengtheningWithNewTerms">
		<xsl:if test="child::node()">
			<tbody id="stnt" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="st"/><h4 style="display: inline-block;">Strengthenings with New Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Weaker Axiom in Ontology 1</th>
					<th class="addition">Strengthened Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Modified Definitions w/ Shared Terms-->
	<xsl:template match="NewModifiedDefinition">
		<xsl:if test="child::node()">
			<tbody id="stequiv" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="aed"/><h4 style="display: inline-block;">Modified Definitions with Shared Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Source Definition in Ontology 1</th>
					<th class="addition">Target Definition in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Modified Definitions w/ New Terms-->
	<xsl:template match="NewModifiedDefinitionWithNewTerms">
		<xsl:if test="child::node()">
			<tbody id="stequivnt" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="aednt"/><h4 style="display: inline-block;">Modified Definitions with New Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Source Definition in Ontology 1</th>
					<th class="addition">Target Definition in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- New Descriptions -->
	<xsl:template match="NewDescription">
		<xsl:if test="child::node()">
			<tbody id="newdesc" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">New Descriptions (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="addition" colspan="2">New Description Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="OneColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Pure Additions w/ Shared Terms -->
	<xsl:template match="PureAddition">
		<xsl:if test="child::node()">
			<tbody id="add" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Pure Additions with Shared Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="addition" colspan="2">Addition to Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="OneColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Pure Additions w/ New Terms -->
	<xsl:template match="PureAdditionWithNewTerms">
		<xsl:if test="child::node()">
			<tbody id="addnt" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="add"/><h4 style="display: inline-block;">Pure Additions with New Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="addition" colspan="2">Addition to Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="OneColumnEffectualAddition"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!--                     EFFECTUAL REMOVALS                     -->
	
	
	<!-- Weakenings w/ Shared Terms -->
	<xsl:template match="Weakening">
		<xsl:if test="child::node()">
			<tbody id="weak" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="weakening"/><h4 style="display: inline-block;">Weakenings with Shared Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Stronger Axiom in Ontology 1</th>
					<th class="normalrem">Weakened Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Weakenings w/ Retired Terms -->
	<xsl:template match="WeakeningWithRetiredTerms">
		<xsl:if test="child::node()">
			<tbody id="weakrt" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="weakening"/><h4 style="display: inline-block;">Weakenings with Retired Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Stronger Axiom in Ontology 1</th>
					<th class="normalrem">Weakened Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Modified Definitions w/ Shared Terms -->
	<xsl:template match="RetiredModifiedDefinition">
		<xsl:if test="child::node()">
			<tbody id="wkequiv" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="redef"/><h4 style="display: inline-block;">Modified Definitions with Shared Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Target Definition in Ontology 1</th>
					<th class="normalrem">Source Definition in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Modified Definitions w/ Retired Terms -->
	<xsl:template match="RetiredModifiedDefinitionWithRetiredTerms">
		<xsl:if test="child::node()">
			<tbody id="wkequivrt" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="redefrt"/><h4 style="display: inline-block;">Modified Definitions with Retired Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Target Definition in Ontology 1</th>
					<th class="normalrem">Source Definition in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="TwoColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Retired Descriptions -->
	<xsl:template match="RetiredDescription">
		<xsl:if test="child::node()">
			<tbody id="retdesc" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Retired Descriptions (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal" colspan="2">Retired Description Axiom in Ontology 1</th>
				</tr>
				<xsl:apply-templates mode="OneColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Pure Removal w/ Shared Terms -->
	<xsl:template match="PureRemoval">
		<xsl:if test="child::node()">
			<tbody id="rem" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Pure Removals with Shared Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal" colspan="2">Removal from Ontology 1</th>
				</tr>
				<xsl:apply-templates mode="OneColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!-- Pure Removal w/ Retired Terms -->
	<xsl:template match="PureRemovalWithRetiredTerms">
		<xsl:if test="child::node()">
			<tbody id="remrt" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="rem"/><h4 style="display: inline-block;">Pure Removals with Retired Terms (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal" colspan="2">Removal from Ontology 1</th>
				</tr>
				<xsl:apply-templates mode="OneColumnEffectualRemoval"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!--                     INEFFECTUAL ADDITIONS                     -->
	
	
	<!--  Redundancy  -->
	<xsl:template match="AddedRedundancy">
		<xsl:if test="child::node()">
			<tbody id="ared" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Complete Rewrite -->
	<xsl:template match="AddedCompleteRewrite">
		<xsl:if test="child::node()">
			<tbody id="arewrite" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Complete Rewrite (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Partial Rewrite -->
	<xsl:template match="AddedPartialRewrite">
		<xsl:if test="child::node()">
			<tbody id="aprw" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Partial Rewrite (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Reshuffle Redundancy 	-->
	<xsl:template match="AddedReshuffleProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="aavred" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="aavred"/><h4 style="display: inline-block;">Reshuffle Restrospective Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- New Redundancy 	-->
	<xsl:template match="AddedNewProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="aweak" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Retrospective New Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Novel Redundancy 	-->
	<xsl:template match="AddedNovelProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="aweak" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Retrospective Novel Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Pseudo Novel Redundancy 	-->
	<xsl:template match="AddedPseudoNovelProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="apseudopred" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Retrospective Pseudo Novel Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="addition">ID</th>
					<th class="normaladd">Justifications in Ontology 1</th>
					<th class="addition">Axiom in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualAdditionTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!--                     INEFFECTUAL REMOVALS                     -->
	
	
	<!-- Redundancy	-->
	<xsl:template match="RemovedRedundancy">
		<xsl:if test="child::node()">
			<tbody id="rred" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Complete Rewrite -->
	<xsl:template match="RemovedCompleteRewrite">
		<xsl:if test="child::node()">
			<tbody id="rrewrite" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Complete Rewrite (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Partial Rewrite -->
	<xsl:template match="RemovedPartialRewrite">
		<xsl:if test="child::node()">
			<tbody id="rprw" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Partial Rewrite (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Reshuffle Redundancy  -->
	<xsl:template match="RemovedReshuffleProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="ravred" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><a name="ravred"/><h4 style="display: inline-block;">Reshuffle Prospective Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- New Redundancy  -->
	<xsl:template match="RemovedNewProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="rst" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Prospective New Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Novel Redundancy  -->
	<xsl:template match="RemovedNovelProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="rst" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Prospective Novel Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>
	
	
	<!-- Pseudo Novel Redundancy  -->
	<xsl:template match="RemovedPseudoNovelProspectiveRedundancy">
		<xsl:if test="child::node()">
			<tbody id="rpsnovred" style="display:none">
				<tr class="withoutstyle">
					<td colspan="3"><h4 style="display: inline-block;">Prospective Pseudo Novel Redundant (<xsl:value-of select="count(./*)"/>)</h4> [<a href="#">back to top</a>]</td>
				</tr>
				<tr>
					<th class="removal">ID</th>
					<th class="removal">Axiom in Ontology 1</th>
					<th class="normalrem">Justifications in Ontology 2</th>
				</tr>
				<xsl:apply-templates mode="IneffectualRemovalTemplate"/>
			</tbody>
		</xsl:if>
	</xsl:template>


	<!--                     CHANGE TEMPLATES                     -->


	<!-- Ineffectual Addition Template -->
	<xsl:template match="Change" mode="IneffectualAdditionTemplate">
		<xsl:variable name="max">
			<xsl:for-each select=".">
				<xsl:sort select="count(Source)" data-type="number"/>
				<xsl:if test="position()=last()">
					<xsl:value-of select="count(Source)"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<tr class="addition">
			<td>
				<xsl:attribute name="rowspan"><xsl:value-of select="$max"/></xsl:attribute>
				<xsl:value-of select="@id"/>
			</td>
			<xsl:for-each select="Source[1]">
				<td>
					<xsl:variable name="counter"><xsl:value-of select="count(Axiom)"/></xsl:variable>
					<xsl:for-each select="Axiom">
						<xsl:variable name="ax2">
							<xsl:call-template name="colorMyAxiom">
								<xsl:with-param name="axiom"><xsl:value-of select="."/></xsl:with-param>
							</xsl:call-template>
						</xsl:variable>
						<i>(<xsl:value-of select="position()"/>)</i>&#160; <xsl:copy-of
							select="$ax2"/>. <xsl:if test="@shared='true'">
							<div style="display:inline;color:#323232" class="hotspot"
								onmouseover="tooltip.show('Shared axiom between Ontology 1 and 2');"
								onmouseout="tooltip.hide();"><sup>[s]</sup>
							</div>
						</xsl:if>
						<xsl:if test="@effectual='true'">
							<div style="display:inline;color:#323232" class="hotspot"
								onmouseover="tooltip.show('Effectual change between Ontology 1 and 2');"
								onmouseout="tooltip.hide();"><sup>[e]</sup>
							</div>
						</xsl:if>
						<xsl:if test="@ineffectual='true'">
							<div style="display:inline;color:#323232" class="hotspot"
								onmouseover="tooltip.show('Ineffectual change between Ontology 1 and 2');"
								onmouseout="tooltip.hide();"><sup>[i]</sup>
							</div>
						</xsl:if>
						<xsl:if test="$counter >1"><br/></xsl:if>
					</xsl:for-each>
				</td>
			</xsl:for-each>
			<td>
				<xsl:attribute name="rowspan"><xsl:value-of select="$max"/></xsl:attribute>
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom"><xsl:value-of select="Axiom"/></xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>. </td>
		</tr>
		<xsl:if test="$max > 1">
			<xsl:for-each select="Source[position() > 1]">
				<tr class="addition">
					<td>
						<xsl:variable name="counter">
							<xsl:value-of select="count(Axiom)"/>
						</xsl:variable>
						<xsl:for-each select="Axiom">
							<xsl:variable name="ax2">
								<xsl:call-template name="colorMyAxiom">
									<xsl:with-param name="axiom"><xsl:value-of select="."/></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<i>(<xsl:value-of select="position()"/>)</i>&#160; <xsl:copy-of
								select="$ax2"/>. <xsl:if test="@shared='true'">
								<div style="display:inline;color:#323232" class="hotspot"
									onmouseover="tooltip.show('Shared axiom between Ontology 1 and 2');"
									onmouseout="tooltip.hide();"><sup>[s]</sup>
								</div>
							</xsl:if>
							<xsl:if test="@effectual='true'">
								<div style="display:inline;color:#323232" class="hotspot"
									onmouseover="tooltip.show('Effectual change between Ontology 1 and 2');"
									onmouseout="tooltip.hide();"><sup>[e]</sup>
								</div>
							</xsl:if>
							<xsl:if test="@ineffectual='true'">
								<div style="display:inline;color:#323232" class="hotspot"
									onmouseover="tooltip.show('Ineffectual change between Ontology 1 and 2');"
									onmouseout="tooltip.hide();"><sup>[i]</sup>
								</div>
							</xsl:if>
							<xsl:if test="$counter >1"><br/></xsl:if>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>


	<!-- Ineffectual Removal Template -->
	<xsl:template match="Change" mode="IneffectualRemovalTemplate">
		<xsl:variable name="max">
			<xsl:for-each select=".">
				<xsl:sort select="count(Source)" data-type="number"/>
				<xsl:if test="position()=last()">
					<xsl:value-of select="count(Source)"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<tr class="removal" >
			<td>
				<xsl:attribute name="rowspan"><xsl:value-of select="$max"/></xsl:attribute>
				<xsl:value-of select="@id"/></td>
			<td>
				<xsl:attribute name="rowspan"><xsl:value-of select="$max"/></xsl:attribute>
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom"><xsl:value-of select="Axiom"/></xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>. 
			</td>
			<xsl:for-each select="Source[1]">
				<td>
					<xsl:variable name="counter"><xsl:value-of select="count(Axiom)"/></xsl:variable>
					<xsl:for-each select="Axiom">
						<xsl:variable name="ax2">
							<xsl:call-template name="colorMyAxiom">
								<xsl:with-param name="axiom"><xsl:value-of select="."/></xsl:with-param>
							</xsl:call-template>
						</xsl:variable>
						<i>(<xsl:value-of select="position()"/>)</i>&#160; <xsl:copy-of select="$ax2"/>. 
						<xsl:if test="@shared='true'">
							<div style="display:inline;color:#323232" class="hotspot" onmouseout="tooltip.hide();"
								onmouseover="tooltip.show('Shared axiom between Ontology 1 and 2');"><sup>[s]</sup>
							</div>
						</xsl:if>
						<xsl:if test="@effectual='true'">
							<div style="display:inline;color:#323232" class="hotspot"
								onmouseover="tooltip.show('Effectual change between Ontology 1 and 2');"
								onmouseout="tooltip.hide();"><sup>[e]</sup>
							</div>
						</xsl:if>
						<xsl:if test="@ineffectual='true'">
							<div style="display:inline;color:#323232" class="hotspot"
								onmouseover="tooltip.show('Ineffectual change between Ontology 1 and 2');"
								onmouseout="tooltip.hide();"><sup>[i]</sup>
							</div>
						</xsl:if>
						<xsl:if test="$counter > 1"><br/></xsl:if>
					</xsl:for-each>
				</td>
			</xsl:for-each>
		</tr>
		<xsl:if test="$max > 1">
			<xsl:for-each select="Source[position() > 1]">
				<tr class="removal">
					<td>
						<xsl:variable name="counter"><xsl:value-of select="count(Axiom)"/></xsl:variable>
						<xsl:for-each select="Axiom">
							<xsl:variable name="ax2">
								<xsl:call-template name="colorMyAxiom">
									<xsl:with-param name="axiom"><xsl:value-of select="."/></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<i>(<xsl:value-of select="position()"/>)</i>&#160; <xsl:copy-of select="$ax2"/>. 
							<xsl:if test="@shared='true'">
								<div style="display:inline;color:#323232" class="hotspot"
									onmouseover="tooltip.show('Shared axiom between Ontology 1 and 2');"
									onmouseout="tooltip.hide();"><sup>[s]</sup></div>
							</xsl:if>
							<xsl:if test="@effectual='true'">
								<div style="display:inline;color:#323232" class="hotspot"
									onmouseover="tooltip.show('Effectual change between Ontology 1 and 2');"
									onmouseout="tooltip.hide();"><sup>[e]</sup>
								</div>
							</xsl:if>
							<xsl:if test="@ineffectual='true'">
								<div style="display:inline;color:#323232" class="hotspot"
									onmouseover="tooltip.show('Ineffectual change between Ontology 1 and 2');"
									onmouseout="tooltip.hide();"><sup>[i]</sup>
								</div>
							</xsl:if>
							<xsl:if test="$counter >1"><br/></xsl:if>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	
	<!-- 1-Column Effectual Addition -->
	<xsl:template match="Change" mode="OneColumnEffectualAddition">
		<tr class="addition">
			<td><xsl:value-of select="@id"/></td>
			<td colspan="2">
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom">
							<xsl:value-of select="Axiom"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>. <br/>
				<xsl:if test="New_Terms">
					<i>New Terms: <xsl:value-of select="New_Terms"/></i>
				</xsl:if>
			</td>
		</tr>
	</xsl:template>
	
	
	<!-- 2-Column Effectual Addition -->
	<xsl:template match="Change" mode="TwoColumnEffectualAddition">
		<tr class="addition">
			<td><xsl:value-of select="@id"/></td>
			<td>
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom"><xsl:value-of select="Source/Axiom"/></xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>. </td>
			<td>
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom"><xsl:value-of select="Axiom"/></xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>.<br/>
				<xsl:if test="New_Terms">
					<i>New Terms: <xsl:value-of select="New_Terms"/></i>
				</xsl:if>
			</td>
		</tr>
	</xsl:template>
	
	
	<!-- 1-Column Effectual Removal -->
	<xsl:template match="Change" mode="OneColumnEffectualRemoval">
		<tr class="removal">
			<td><xsl:value-of select="@id"/></td>
			<td colspan="2">
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom">
							<xsl:value-of select="Axiom"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>.<br/>
				<xsl:if test="Retired_Terms">
					<i>Retired Terms: <xsl:value-of select="Retired_Terms"/></i>
				</xsl:if>
			</td>
		</tr>
	</xsl:template>
	
	
	<!-- 2-Column Effectual Removal -->
	<xsl:template match="Change" mode="TwoColumnEffectualRemoval">
		<tr class="removal">
			<td><xsl:value-of select="@id"/></td>
			<td>
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom">
							<xsl:value-of select="Axiom"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>. 
			</td>
			<td>
				<xsl:variable name="myAxiom">
					<xsl:call-template name="colorMyAxiom">
						<xsl:with-param name="axiom">
							<xsl:value-of select="Source/Axiom"/>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:copy-of select="$myAxiom"/>.<br/>
				<xsl:if test="Retired_Terms">
					<i>Retired Terms: <xsl:value-of select="Retired_Terms"/></i>
				</xsl:if>
			</td>
		</tr>
	</xsl:template>


	<!--                     AXIOM HANDLING TEMPLATES                     -->


	<!-- Find and Color -->
	<xsl:template name="colorMyAxiom">
		<xsl:param name="axiom"/>
		<xsl:analyze-string select="$axiom"
			regex="( SubClassOf | EquivalentTo | SubPropertyOf | Range |Range: | Domain |Domain: | Type |DisjointClasses: |Functional: |InverseFunctional: |Reflexive: 
			|Irreflexive: |Symmetric: |Asymmetric: |Transitive: | DisjointWith | InverseOf | DifferentIndividuals: | SameAs | DifferentFrom )|( some | only | value | 
			min | max | exactly | Self )|( and | or )|(not )">
			<xsl:matching-substring>
				<xsl:if test="not(string-length(regex-group(1)) = 0)">
					<span class="subclass">
						<xsl:value-of select="regex-group(1)"/>
					</span>
				</xsl:if>
				<xsl:if test="not(string-length(regex-group(2)) = 0)">
					<span class="some">
						<xsl:value-of select="regex-group(2)"/>
					</span>
				</xsl:if>
				<xsl:if test="not(string-length(regex-group(3)) = 0)">
					<span class="keyword">
						<xsl:value-of select="regex-group(3)"/>
					</span>
				</xsl:if>
				<xsl:if test="not(string-length(regex-group(4)) = 0)">
					<span class="keyword">
						<xsl:value-of select="regex-group(4)"/>
					</span>
				</xsl:if>
			</xsl:matching-substring>
			<xsl:non-matching-substring>
				<xsl:value-of select="."/>
			</xsl:non-matching-substring>
		</xsl:analyze-string>
	</xsl:template>
</xsl:stylesheet>