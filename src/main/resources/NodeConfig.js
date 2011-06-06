$(document).ready(function() {

	
	$("#reset").click(function(event) {
		$.ajax({
			url : "?reset",
			type : 'POST',
			context : document.body,
			success : function(data, textStatus, jqXHR) {
				var result = eval('(' + jqXHR.responseText + ')');
				
			},
			error : function(jqXHR, textStatus, errorThrown) {
				alert("Affreux. " + jqXHR.responseText);
			}
		}); // end ajax
	});
	
	$(".validate_cap").click(function(event) {
		var index = $(this).attr('index');
		$.ajax({
			url : "?validate="+index,
			type : 'POST',
			context : document.body,
			success : function(data, textStatus, jqXHR) {
				var result = eval('(' + jqXHR.responseText + ')');
				console.log(result);
			},
			error : function(jqXHR, textStatus, errorThrown) {
				alert("Affreux. " + jqXHR.responseText);
			}
		}); // end ajax
		
	});
	
	

	var old;
	$("#browserLocation").keypress(function(event) {
		var keyCode = event.keyCode || event.which;
		if (keyCode == 9) {
			path = $(this).val();
			event.preventDefault();
			if (path != old) {
				$.ajax({
					url : "?completion=" + path,
					type : 'POST',
					context : document.body,
					success : function(data, textStatus, jqXHR) {
						var result = eval('(' + jqXHR.responseText + ')');
						$("#info").html(result.info);
						$("#browserLocation").val(result.content);
						validatePath(result.content);
						old = result.content;
						if (result.isDirectory) {
							old = 'ferret';
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						alert("Affreux. " + jqXHR.responseText);
					}
				}); // end ajax
			}

		} else if (keyCode == 13) {
			var path = $(this).val();
			event.preventDefault();
			$.ajax({
				url : "?submit=" + path,
				type : 'POST',
				context : document.body,
				success : function(data, textStatus, jqXHR) {
					var result = eval('(' + jqXHR.responseText + ')');
					if (result.success) {
						$("#browserLocation").val('');
					}
					$("#info").html(result.info);
					$("#capabilities").html(result.content);

				},
				error : function(jqXHR, textStatus, errorThrown) {
					alert("Affreux. " + jqXHR.responseText);
				}
			}); // end ajax
		}
	}); // end keypress

	$("#browserLocation").keyup(function(event) {
		var path = $(this).val();
		validatePath(path);
	}); // end keyup

	function validatePath(path) {
		$.ajax({
			url : "?current=" + path,
			type : 'POST',
			context : document.body,
			success : function(data, textStatus, jqXHR) {
				var result = eval('(' + jqXHR.responseText + ')');
				if (result.success) {
					$('#info').html(result.content);
				}

			},
			error : function(jqXHR, textStatus, errorThrown) {
				$('#info').html(jqXHR.responseText);
			}
		}); // end ajax
	}

});
