function lastChildNum() {
    let root = $('#configList');
    if (root) {
        return root.children().length;
    }
    return undefined;
}

function currentChildNum() {
    let root = $('#configList');
    if (root) {
        let numOfChild = 1;
        for (let child of root.children()) {
            if (child.classList.contains("active"))
                return numOfChild;
            numOfChild++;
        }
    }
    return undefined;
}

function findElementNum(elem) {
    let root = $('#configList');
    if (root) {
        let numOfChild = 1;
        for (let child of root.children()) {
            if (child === elem)
                return numOfChild;
            numOfChild++;
        }
    }
    return undefined;
}

function nextChild() {
    let nval = currentChildNum();
    if (nval !== undefined) {
        $('#configList a:nth-child(' + (nval + 1) + ')').tab('show');
        handleChangeChild(nval, nval + 1);
    }
}

function prevChild() {
    let nval = currentChildNum();
    if (nval !== undefined) {
        $('#configList a:nth-child(' + (nval - 1) + ')').tab('show');
        handleChangeChild(nval, nval - 1);
    }
}

function handleChangeChild(currentNum, newNum) {
    if (currentNum !== newNum) {
        var orderId = $('#orderId').val();
        var processId = $('#processId').val();
        var taskId = $('#taskId').val();
        var client = $('#client').val();
        var model = $('#modelSelect option:selected').val();
        var engine =  $('#engineSelect option:selected').val();
        var bodyType =  $('#bodyTypeSelect option:selected').val();
        var transmissionType =  $('#transmissionTypeSelect option:selected').val();
        var additionalEquipments = []
        $('#equipmentsDiv input:checked').each(function() {
            additionalEquipments.push($(this).attr('value'));
        });


        var dataToSend = {};
        dataToSend["orderId"] = orderId;
        dataToSend["processId"] = processId;
        dataToSend["taskId"] = taskId;
        dataToSend["client"] = client;
        dataToSend["model"] = model;
        dataToSend["engine"] = engine;
        dataToSend["bodyType"] = bodyType;
        dataToSend["transmissionType"] = transmissionType;
        dataToSend["additionalEquipments"] = additionalEquipments

        let urlStr = ((currentNum+1) !== newNum)
            ? "/client/change/" + processId + "?stepNum=" + newNum
            : "/client/complete/" + taskId;

        $('#ajaxError').hide();
        $('#form').hide();
        $('#spinner').show();

        $.ajax({
            type: "POST",
            contentType: "application/json",
            url: urlStr,
            data: JSON.stringify(dataToSend),
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (d) {
                $('#form').show();
                $('#spinner').hide();
                //console.log("SUCCESS", d);
                $('#orderId').val(d.orderId);
                $('#processId').val(d.processId);
                $('#taskId').val(d.taskId);
                $('#client').val(d.client);
                $("#modelSelect").val(d.model).change();
                $("#engineSelect").val(d.engine).change();
                $("#bodyTypeSelect").val(d.bodyType).change();
                $("#transmissionTypeSelect").val(d.transmissionType).change();

                $('#equipmentsDiv input').each(function() {
                    $(this).attr('checked', d.additionalEquipments.includes($(this).attr('value')));
                });

            },
            error: function (e) {
                $('#ajaxError').show();
                $('#submitOrder').attr('disabled', true);

                $('#form').show();
                $('#spinner').hide();

                console.log("ERROR", e);
            }
        });


        //Podsumowanie
        if (newNum === lastChildNum()) {
            $("#summaryTable > tbody tr").remove();

            $('#summaryTable tbody').append('<tr> <td>Klient</td> <td>' + client + '</td> </tr>');
            $('#summaryTable tbody').append('<tr> <td>Model</td> <td>' + model + '</td> </tr>');
            $('#summaryTable tbody').append('<tr> <td>Silnik</td> <td>' + engine + '</td> </tr>');
            $('#summaryTable tbody').append('<tr> <td>Rodzaj nadwozia</td> <td>' + bodyType + '</td> </tr>');
            $('#summaryTable tbody').append('<tr> <td>Typ skrzyni biegów</td> <td>' + transmissionType + '</td> </tr>');
            $('#summaryTable tbody').append('<tr> <td>Dodatkowe wyposażenie</td> <td>' + additionalEquipments.join(", ") + '</td> </tr>');

            if (model && engine && bodyType && transmissionType) {
                $('#submitOrder').attr('disabled', false);
            } else {
                var fields = '';
                if (!model)
                    fields += 'Model';

                if (!engine) {
                    if (fields) fields += ', ';
                    fields += 'Silnik';
                }
                if (!bodyType) {
                    if (fields) fields += ', ';
                    fields += 'Typ nadwozia';
                }
                if (!transmissionType) {
                    if (fields) fields += ', ';
                    fields += 'Typ skrzyni biegów';
                }

                fields = fields.replace(/,(?=[^,]*$)/, ' i');

                $('#summaryTable tbody').append('<tr class="text-danger text-center"> <td colspan="2">Nie wypełniono wymaganych pól: <strong>'+ fields + '</strong>!</td> </tr>');
                $('#submitOrder').attr('disabled', true);
            }
        }
    }
}