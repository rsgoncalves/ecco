/*******************************************************************************
 * This file is part of ecco.
 *
 * ecco is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0.
 *
 * Copyright 2011-2013, The University of Manchester
 *
 * ecco is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * ecco is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ecco.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/

$(document).ready(function() {
	$('#tree1').checkboxTree({
		initializeChecked: 'expanded',
		initializeUnchecked: 'collapsed',
		collapseDuration: 50,
		expandDuration: 100,
		collapseImage: 'images/minus.png',
		expandImage: 'images/plus.png',
		onCheck: {
			node: 'expand',
			ancestors: 'checkIfFull',
			descendants: 'check' },
			onUncheck: {
				node: 'collapse',
				ancestors: 'uncheck' }
	});
	$('#tree2').checkboxTree({
		initializeChecked: 'expanded',
		initializeUnchecked: 'collapsed',
		collapseDuration: 50,
		expandDuration: 100,
		collapseImage: 'images/minus.png',
		expandImage: 'images/plus.png',
		onCheck: {
			node: 'expand',
			ancestors: 'checkIfFull',
			descendants: 'check' },
			onUncheck: {
				node: 'collapse',
				ancestors: 'uncheck' }
	});
	$('#tree1-expandAll').click(function(){
		$('#tree1').checkboxTree('expandAll');
	});
	$('#tree1-collapseAll').click(function(){
		$('#tree1').checkboxTree('collapseAll');
	});
	$('#tree2-expandAll').click(function(){
		$('#tree2').checkboxTree('expandAll');
	});
	$('#tree2-collapseAll').click(function(){
		$('#tree2').checkboxTree('collapseAll');
	});
	$('#genViewPerml').click(function(e) {
		e.preventDefault();
		$('#view-plink-modal').reveal({
			animation: 'none',
			animationspeed: 100,
			closeonbackgroundclick: true,
			dismissmodalclass: 'close-reveal-modal'
		});
	});
});